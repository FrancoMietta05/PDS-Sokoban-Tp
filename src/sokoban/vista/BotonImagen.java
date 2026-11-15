package sokoban.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;

/**
 * Boton del HUD. Si existe assets/imagenes/<nombreImagen>.png dibuja esa
 * imagen (el diseno ya trae el texto); si no, dibuja una chapa de madera con la
 * etiqueta, para que el juego sea jugable antes de tener los disenos.
 *
 * No toma el foco del teclado: asi las flechas siguen yendo al tablero despues
 * de hacer click.
 */
public class BotonImagen extends JButton {

    private final Image imagen;
    private final String etiqueta;

    public BotonImagen(String etiqueta, String nombreImagen) {
        this.etiqueta = etiqueta;
        this.imagen = cargar(nombreImagen);
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    private static Image cargar(String nombre) {
        try {
            File archivo = new File("assets/imagenes/" + nombre + ".png");
            return archivo.exists() ? ImageIO.read(archivo) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int desplazamiento = getModel().isPressed() ? Math.max(1, getHeight() / 28) : 0;
        g2.translate(0, desplazamiento);
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }

        if (imagen != null) {
            dibujarImagen(g2);
        } else {
            dibujarChapa(g2);
        }
        if (isEnabled() && getModel().isRollover() && !getModel().isPressed()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
        }
        g2.dispose();
    }

    private void dibujarImagen(Graphics2D g2) {
        double k = Math.min((double) getWidth() / imagen.getWidth(null),
                (double) getHeight() / imagen.getHeight(null));
        int w = (int) Math.round(imagen.getWidth(null) * k);
        int h = (int) Math.round(imagen.getHeight(null) * k);
        g2.drawImage(imagen, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
    }

    private void dibujarChapa(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        int arco = Math.max(8, h / 4);
        g2.setColor(new Color(96, 62, 36));
        g2.fillRoundRect(0, 0, w, h, arco, arco);
        g2.setColor(new Color(128, 86, 50));
        g2.fillRoundRect(3, 3, w - 6, h - 6, arco, arco);
        g2.setColor(Estilo.BORDE_CLARO);
        g2.drawRoundRect(1, 1, w - 3, h - 3, arco, arco);
        g2.setColor(Estilo.SOMBRA);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arco, arco);

        float tamano = Math.max(8f, h * 0.26f);
        int y = (h + Math.round(tamano)) / 2 - 1;
        Estilo.textoCentrado(g2, etiqueta, 8, w - 16, y, tamano, Estilo.TEXTO);
    }
}
