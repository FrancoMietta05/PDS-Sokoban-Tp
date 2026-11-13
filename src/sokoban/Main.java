package sokoban;

import javax.swing.SwingUtilities;

import sokoban.vista.VentanaPrincipal;

/** Punto de entrada. Levanta la ventana en el hilo de eventos de Swing. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
