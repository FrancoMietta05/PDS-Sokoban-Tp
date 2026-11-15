import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import sokoban.vista.VentanaPrincipal;

/**
 * Prueba de regresion del bug de foco.
 *
 * El KeyListener original dejaba de recibir teclas en cuanto se hacia clic en
 * un boton del HUD: el foco se iba al boton y el juego no respondia mas a las
 * flechas. Se arreglo con key bindings (WHEN_IN_FOCUSED_WINDOW).
 *
 * A diferencia de PruebaSokoban, esta necesita pantalla: usa Robot para
 * clickear y teclear de verdad sobre la ventana.
 */
public class PruebaFoco {
    static VentanaPrincipal v;
    static int fallas = 0;

    static void check(String q, boolean ok) {
        System.out.println((ok ? "  OK   " : "  FALLA") + "  " + q);
        if (!ok) fallas++;
    }

    static JButton boton(Container c, String texto) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton && ((JButton) comp).getText().contains(texto)) return (JButton) comp;
            if (comp instanceof Container) { JButton r = boton((Container) comp, texto); if (r != null) return r; }
        }
        return null;
    }

    static JLabel label(Container c, String texto) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith(texto)) return (JLabel) comp;
            if (comp instanceof Container) { JLabel r = label((Container) comp, texto); if (r != null) return r; }
        }
        return null;
    }

    static void clic(Robot r, JButton b) throws Exception {
        Point p = b.getLocationOnScreen();
        r.mouseMove(p.x + b.getWidth()/2, p.y + b.getHeight()/2);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(400);
    }

    static void tecla(Robot r, int code) throws Exception {
        r.keyPress(code); r.keyRelease(code); Thread.sleep(250);
    }

    static int movimientos() {
        JLabel l = label(v, "Movimientos");
        return Integer.parseInt(l.getText().replaceAll("[^0-9]+", ""));
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> { v = new VentanaPrincipal(); v.setVisible(true); });
        Thread.sleep(1000);
        Robot r = new Robot();

        tecla(r, KeyEvent.VK_LEFT);
        check("se mueve con el teclado al arrancar", movimientos() == 1);

        JButton reiniciar = boton(v, "Reiniciar");
        clic(r, reiniciar);
        check("reiniciar puso los movimientos en 0", movimientos() == 0);
        tecla(r, KeyEvent.VK_LEFT);
        check("SE MUEVE DESPUES DE REINICIAR", movimientos() == 1);

        tecla(r, KeyEvent.VK_UP);
        tecla(r, KeyEvent.VK_RIGHT);
        int antes = movimientos();
        JButton undo = boton(v, "Deshacer");
        clic(r, undo);
        tecla(r, KeyEvent.VK_LEFT);
        check("SE MUEVE DESPUES DE DESHACER", movimientos() != antes);

        check("el foco sigue en el tablero", String.valueOf(
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner())
                .contains("PanelTablero"));

        System.out.println(fallas == 0 ? "\nTODO OK" : "\n" + fallas + " FALLAS");
        System.exit(fallas == 0 ? 0 : 1);
    }
}
