package sokoban.vista;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JPanel;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;
import sokoban.modelo.memento.HistorialMovimientos;

/**
 * HUD: cartel colgante con nivel, movimientos, empujes y estado de power-ups.
 * Ademas es duenio de los botones de deshacer y reiniciar que pide el
 * enunciado; la ventana los ubica sobre el fondo (ver VentanaPrincipal).
 *
 * Es observador: no pregunta cada tanto si algo cambio, se entera cuando
 * cambia y relee los contadores del controlador. Los botones van en la otra
 * direccion -vista hacia controlador- llamando a los metodos de Juego.
 */
public class PanelHUD extends JPanel implements Observador {

    private final Juego juego;

    private final BotonImagen btnUndo = new BotonImagen("DESHACER", "btn_deshacer");
    private final BotonImagen btnReiniciar = new BotonImagen("REINICIAR", "btn_reiniciar");

    private int nivel;
    private int movimientos;
    private int empujes;
    private String estado = "";

    public PanelHUD(Juego juego) {
        this.juego = juego;
        setOpaque(false);

        btnUndo.addActionListener(e -> juego.deshacerMovimiento());
        btnReiniciar.addActionListener(e -> juego.reiniciarNivel());

        refrescar();
    }

    public JButton getBotonDeshacer() {
        return btnUndo;
    }

    public JButton getBotonReiniciar() {
        return btnReiniciar;
    }

    @Override
    public void actualizar(EventoJuego e) {
        refrescar();
    }

    public void refrescar() {
        nivel = juego.getNivelActual();
        movimientos = juego.getMovimientos();
        empujes = juego.getEmpujes();
        estado = juego.getJugador() == null ? "" : juego.getJugador().descripcion();

        btnUndo.setEnabled(juego.puedeDeshacer());
        int restantes = HistorialMovimientos.MAX_USOS_CONSECUTIVOS
                - juego.getHistorial().getUsosConsecutivos();
        btnUndo.setToolTipText("Retrocede 5 movimientos. Usos consecutivos restantes: "
                + Math.max(0, restantes));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arco = Math.max(8, h / 6);
        int borde = Math.max(2, h / 28);

        // Cartel: marco de madera + interior oscuro.
        g2.setColor(Estilo.BORDE);
        g2.fillRoundRect(0, 0, w, h, arco, arco);
        g2.setColor(Estilo.PANEL);
        g2.fillRoundRect(borde, borde, w - 2 * borde, h - 2 * borde, arco / 2, arco / 2);
        g2.setColor(Estilo.BORDE_CLARO);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arco, arco);

        float chica = Math.max(8f, h * 0.15f);
        float grande = Math.max(10f, h * 0.25f);
        int margen = w / 20;
        int columna = (w - 2 * margen) / 3;

        String[] etiquetas = {"NIVEL", "MOVIMIENTOS", "EMPUJES"};
        String[] valores = {String.valueOf(nivel), String.valueOf(movimientos), String.valueOf(empujes)};
        for (int i = 0; i < 3; i++) {
            int x = margen + i * columna;
            Estilo.textoCentrado(g2, etiquetas[i], x, columna, Math.round(h * 0.28f), chica, Estilo.ETIQUETA);
            Estilo.textoCentrado(g2, valores[i], x, columna, Math.round(h * 0.65f), grande, Estilo.TEXTO);
        }

        // Separador y linea de estado (power-ups activos).
        g2.setColor(Estilo.BORDE);
        int ySep = Math.round(h * 0.75f);
        g2.fillRect(margen, ySep, w - 2 * margen, Math.max(1, h / 60));
        String linea = estado == null || estado.isEmpty() ? "SIN POWER-UPS" : estado.toUpperCase();
        Estilo.textoCentrado(g2, linea, margen, w - 2 * margen, Math.round(h * 0.93f), chica, Estilo.TEXTO);
        g2.dispose();
    }
}
