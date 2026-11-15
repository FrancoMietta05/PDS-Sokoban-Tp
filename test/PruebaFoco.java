import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import sokoban.controlador.Juego;
import sokoban.vista.PanelHUD;
import sokoban.vista.VentanaPrincipal;

/**
 * Prueba de regresion del bug de foco.
 *
 * El KeyListener original dejaba de recibir teclas en cuanto se hacia clic en
 * un boton del HUD: el foco se iba al boton y el juego no respondia mas a las
 * flechas. Se arreglo con key bindings (WHEN_IN_FOCUSED_WINDOW) y dejando los
 * botones del HUD sin foco.
 *
 * A diferencia de PruebaSokoban, esta necesita pantalla: usa Robot para
 * clickear y teclear de verdad sobre la ventana.
 *
 * Los contadores se leen del Juego, no de la interfaz. Antes se buscaba un
 * JLabel del HUD por su texto, y la prueba se rompio sola cuando el HUD paso
 * a dibujar los valores con drawString. Lo que esta prueba vigila es que la
 * tecla llegue al controlador; como la vista muestre el numero es otro asunto.
 */
public class PruebaFoco {

    static VentanaPrincipal ventana;
    static Juego juego;
    static int fallas = 0;

    static void check(String que, boolean ok) {
        System.out.println((ok ? "  OK   " : "  FALLA") + "  " + que);
        if (!ok) fallas++;
    }

    /**
     * Ubica el PanelHUD en el arbol de componentes y le pide sus botones por
     * su API publica. Buscarlos por texto no sirve: BotonImagen dibuja su
     * etiqueta por su cuenta y getText() devuelve vacio.
     */
    static PanelHUD buscarHud(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof PanelHUD) {
                return (PanelHUD) comp;
            }
            if (comp instanceof Container) {
                PanelHUD r = buscarHud((Container) comp);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    static void clic(Robot r, JButton b) throws Exception {
        Point p = b.getLocationOnScreen();
        r.mouseMove(p.x + b.getWidth() / 2, p.y + b.getHeight() / 2);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(400);
    }

    static void tecla(Robot r, int code) throws Exception {
        r.keyPress(code);
        r.keyRelease(code);
        Thread.sleep(250);
    }

    public static void main(String[] args) throws Exception {
        Juego.reiniciarInstancia();
        SwingUtilities.invokeAndWait(() -> {
            ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
        juego = Juego.getInstance();
        Thread.sleep(1000);
        Robot r = new Robot();

        tecla(r, KeyEvent.VK_LEFT);
        check("se mueve con el teclado al arrancar", juego.getMovimientos() == 1);

        PanelHUD hud = buscarHud(ventana);
        check("se encontro el PanelHUD en la ventana", hud != null);
        JButton reiniciar = hud.getBotonReiniciar();
        clic(r, reiniciar);
        check("reiniciar puso los movimientos en cero", juego.getMovimientos() == 0);
        tecla(r, KeyEvent.VK_LEFT);
        check("SE MUEVE DESPUES DE REINICIAR", juego.getMovimientos() == 1);

        tecla(r, KeyEvent.VK_UP);
        tecla(r, KeyEvent.VK_RIGHT);
        int antes = juego.getMovimientos();
        JButton undo = hud.getBotonDeshacer();
        clic(r, undo);
        tecla(r, KeyEvent.VK_LEFT);
        check("SE MUEVE DESPUES DE DESHACER", juego.getMovimientos() != antes);

        check("los botones del HUD no roban el foco",
                !reiniciar.isFocusable() && !undo.isFocusable());
        check("el foco sigue en el tablero", String.valueOf(
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner())
                .contains("PanelTablero"));

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : fallas + " FALLAS");
        System.exit(fallas == 0 ? 0 : 1);
    }
}
