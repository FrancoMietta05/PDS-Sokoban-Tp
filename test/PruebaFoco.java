import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
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

    /** Cuantas veces hubo que traer la ventana al frente porque otra aplicacion tenia el foco. */
    static int reactivaciones = 0;

    /**
     * Robot no le manda las teclas a nuestra ventana: las inyecta en Windows,
     * que se las entrega a la ventana que este al frente. Si en ese momento el
     * usuario esta usando otra aplicacion, las flechas van a parar ahi y la
     * prueba falla aunque el juego ande bien.
     *
     * Por eso, antes de cada tecla, si la ventana no es la activa se la activa
     * con un clic en la barra de titulo. No se clickea el tablero: eso le
     * devolveria el foco al PanelTablero y esconderia justo el bug que esta
     * prueba vigila (el foco que se queda en un boton).
     */
    static void asegurarVentanaActiva(Robot r) throws Exception {
        for (int intento = 0; intento < 10 && !activa(); intento++) {
            reactivaciones++;
            SwingUtilities.invokeAndWait(() -> ventana.toFront());
            Point p = ventana.getLocationOnScreen();
            Insets bordes = ventana.getInsets();
            r.mouseMove(p.x + ventana.getWidth() / 2, p.y + Math.max(4, bordes.top / 2));
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(300);
        }
        if (!activa()) {
            throw new IllegalStateException("La ventana del juego no consiguio el foco de Windows:"
                    + " otra aplicacion lo tiene. Volve a correr la prueba sin tocar nada.");
        }
    }

    static boolean activa() throws Exception {
        boolean[] activa = new boolean[1];
        SwingUtilities.invokeAndWait(() -> activa[0] = ventana.isActive());
        return activa[0];
    }

    static void tecla(Robot r, int code) throws Exception {
        asegurarVentanaActiva(r);
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

        if (reactivaciones > 0) {
            System.out.println("  (otra aplicacion tenia el foco: la ventana se trajo al frente "
                    + reactivaciones + " vez/veces)");
        }
        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : fallas + " FALLAS");
        System.exit(fallas == 0 ? 0 : 1);
    }
}
