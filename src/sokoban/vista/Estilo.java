package sokoban.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;

/**
 * Identidad visual compartida por los paneles del HUD: colores, tipografia y un
 * helper para dibujar texto con sombra (legible sobre cualquier fondo).
 *
 * La tipografia es "Press Start 2P" (pixel art, licencia OFL) y se carga de
 * assets/fuentes. Si el archivo no esta, se cae a una fuente monoespaciada del
 * sistema para que el juego siga andando.
 */
final class Estilo {

    static final Color TEXTO = new Color(255, 238, 180);
    static final Color ETIQUETA = new Color(255, 190, 70);
    static final Color SOMBRA = new Color(18, 10, 8);
    static final Color PANEL = new Color(28, 20, 22, 225);
    static final Color BORDE = new Color(150, 104, 58);
    static final Color BORDE_CLARO = new Color(214, 160, 96);

    private static final String ARCHIVO_FUENTE = "assets/fuentes/PressStart2P-Regular.ttf";
    private static final Font BASE = cargarBase();

    private Estilo() {
    }

    private static Font cargarBase() {
        File archivo = new File(ARCHIVO_FUENTE);
        if (archivo.exists()) {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, archivo);
            } catch (Exception e) {
                // cae a la fuente del sistema
            }
        }
        return null;
    }

    /** Fuente pixel al tamano pedido; si no hay archivo, una monoespaciada en negrita. */
    static Font fuente(float tamano) {
        if (BASE == null) {
            return new Font(Font.MONOSPACED, Font.BOLD, Math.round(tamano));
        }
        return BASE.deriveFont(tamano);
    }

    /**
     * Dibuja el texto con sombra, achicando la fuente hasta que entre en
     * anchoMax. Devuelve el ancho dibujado.
     */
    static int texto(Graphics2D g2, String texto, int x, int y, float tamano, int anchoMax, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        float t = tamano;
        Font f = fuente(t);
        if (f.canDisplayUpTo(texto) != -1) {
            f = new Font(Font.MONOSPACED, Font.BOLD, Math.round(t));
        }
        FontMetrics fm = g2.getFontMetrics(f);
        while (fm.stringWidth(texto) > anchoMax && t > 7f) {
            t -= 1f;
            f = f.deriveFont(t);
            fm = g2.getFontMetrics(f);
        }
        int ancho = fm.stringWidth(texto);
        int sombra = Math.max(1, Math.round(t / 8f));
        g2.setFont(f);
        g2.setColor(SOMBRA);
        g2.drawString(texto, x + sombra, y + sombra);
        g2.setColor(color);
        g2.drawString(texto, x, y);
        return ancho;
    }

    /** Igual que texto(), pero centrado horizontalmente en [x, x + ancho]. */
    static void textoCentrado(Graphics2D g2, String texto, int x, int ancho, int y, float tamano, Color color) {
        Font f = fuente(tamano);
        if (f.canDisplayUpTo(texto) != -1) {
            f = new Font(Font.MONOSPACED, Font.BOLD, Math.round(tamano));
        }
        float t = tamano;
        FontMetrics fm = g2.getFontMetrics(f);
        while (fm.stringWidth(texto) > ancho && t > 7f) {
            t -= 1f;
            f = f.deriveFont(t);
            fm = g2.getFontMetrics(f);
        }
        int w = fm.stringWidth(texto);
        texto(g2, texto, x + (ancho - w) / 2, y, t, ancho, color);
    }
}
