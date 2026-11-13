package sokoban.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;
import sokoban.logros.Logro;

/**
 * Franja donde aparecen los avisos de logro desbloqueado.
 *
 * Solo reacciona a LOGRO_DESBLOQUEADO y toma el logro del mapa de datos del
 * evento. No conoce al GestorLogros: se entera por el Observer.
 */
public class PanelNotificaciones extends JPanel implements Observador {

    private static final int MS_VISIBLE = 4000;

    private final JLabel lblAviso = new JLabel(" ");
    private final Timer temporizador;

    public PanelNotificaciones() {
        setLayout(new GridLayout(1, 1));
        setBackground(new Color(38, 38, 46));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        lblAviso.setForeground(new Color(250, 214, 110));
        lblAviso.setFont(lblAviso.getFont().deriveFont(Font.BOLD, 14f));
        add(lblAviso);

        temporizador = new Timer(MS_VISIBLE, e -> lblAviso.setText(" "));
        temporizador.setRepeats(false);
    }

    @Override
    public void actualizar(EventoJuego e) {
        if (e.getTipo() != TipoEvento.LOGRO_DESBLOQUEADO) {
            return;
        }
        Object dato = e.getDato(EventoJuego.CLAVE_LOGRO);
        if (dato instanceof Logro) {
            mostrarLogro((Logro) dato);
        }
    }

    public void mostrarLogro(Logro logro) {
        lblAviso.setText("Logro desbloqueado: " + logro.getNombre() + " - " + logro.getDescripcion());
        temporizador.restart();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferida = super.getPreferredSize();
        return new Dimension(preferida.width, Math.max(34, preferida.height));
    }
}
