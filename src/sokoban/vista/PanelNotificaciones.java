package sokoban.vista;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.Timer;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;
import sokoban.logros.Logro;

/**
 * Cartel que aparece unos segundos cuando se desbloquea un logro. Sin aviso no
 * dibuja nada, asi que no tapa el fondo.
 *
 * Solo reacciona a LOGRO_DESBLOQUEADO y toma el logro del mapa de datos del
 * evento. No conoce al GestorLogros: se entera por el Observer.
 */
public class PanelNotificaciones extends JPanel implements Observador {

    private static final int MS_VISIBLE = 4000;

    private String aviso = null;
    private final Timer temporizador;

    public PanelNotificaciones() {
        setOpaque(false);
        temporizador = new Timer(MS_VISIBLE, e -> {
            aviso = null;
            repaint();
        });
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
        aviso = "LOGRO: " + logro.getNombre() + " - " + logro.getDescripcion();
        temporizador.restart();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (aviso == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arco = Math.max(8, h / 2);
        g2.setColor(Estilo.BORDE);
        g2.fillRoundRect(0, 0, w, h, arco, arco);
        g2.setColor(Estilo.PANEL);
        g2.fillRoundRect(3, 3, w - 6, h - 6, arco - 4, arco - 4);

        float tamano = Math.max(8f, h * 0.30f);
        Estilo.textoCentrado(g2, aviso, arco / 2, w - arco, (h + Math.round(tamano)) / 2 - 1,
                tamano, Estilo.ETIQUETA);
        g2.dispose();
    }
}
