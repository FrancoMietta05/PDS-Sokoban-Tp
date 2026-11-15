package sokoban.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;

/**
 * HUD: nivel, movimientos y empujes, mas los botones de deshacer y reiniciar
 * que pide el enunciado.
 *
 * Es observador: no pregunta cada tanto si algo cambio, se entera cuando
 * cambia y relee los contadores del controlador. Los botones van en la otra
 * direccion -vista hacia controlador- llamando a los metodos de Juego.
 */
public class PanelHUD extends JPanel implements Observador {

    private final Juego juego;

    private final JLabel lblNivel = new JLabel();
    private final JLabel lblMovimientos = new JLabel();
    private final JLabel lblEmpujes = new JLabel();
    private final JLabel lblPowerUps = new JLabel();
    private final JButton btnUndo = new JButton("Deshacer (5)");
    private final JButton btnReiniciar = new JButton("Reiniciar nivel");
    private final JButton btnPartidaNueva = new JButton("Partida nueva");

    /**
     * @param partidaNueva que hacer al pedir una partida nueva. Lo resuelve
     *        VentanaPrincipal, que es la que conoce a GestorLogros: el HUD no
     *        necesita saber que los logros se borran.
     */
    public PanelHUD(Juego juego, Runnable partidaNueva) {
        this.juego = juego;
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        setBackground(new Color(38, 38, 46));
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        for (JLabel etiqueta : new JLabel[]{lblNivel, lblMovimientos, lblEmpujes, lblPowerUps}) {
            etiqueta.setForeground(new Color(232, 232, 238));
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 14f));
            add(etiqueta);
        }

        add(Box.createHorizontalStrut(12));
        add(btnUndo);
        add(btnReiniciar);
        add(btnPartidaNueva);

        // Los botones no toman el foco: asi el tablero lo conserva y no aparece
        // el recuadro de foco saltando entre botones mientras se juega.
        btnUndo.setFocusable(false);
        btnReiniciar.setFocusable(false);
        btnPartidaNueva.setFocusable(false);

        btnUndo.addActionListener(e -> juego.deshacerMovimiento());
        btnReiniciar.addActionListener(e -> juego.reiniciarNivel());
        btnPartidaNueva.addActionListener(e -> partidaNueva.run());

        refrescar();
    }

    @Override
    public void actualizar(EventoJuego e) {
        refrescar();
    }

    public void refrescar() {
        lblNivel.setText("Nivel: " + juego.getNivelActual());
        lblMovimientos.setText("Movimientos: " + juego.getMovimientos());
        lblEmpujes.setText("Empujes: " + juego.getEmpujes());

        String descripcion = juego.getJugador() == null ? "" : juego.getJugador().descripcion();
        lblPowerUps.setText("Estado: " + descripcion);

        btnUndo.setEnabled(juego.puedeDeshacer());
        int restantes = sokoban.modelo.memento.HistorialMovimientos.MAX_USOS_CONSECUTIVOS
                - juego.getHistorial().getUsosConsecutivos();
        btnUndo.setToolTipText("Retrocede 5 movimientos. Usos consecutivos restantes: "
                + Math.max(0, restantes));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferida = super.getPreferredSize();
        return new Dimension(preferida.width, Math.max(48, preferida.height));
    }
}
