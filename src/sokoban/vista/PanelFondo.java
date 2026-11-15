package sokoban.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Panel raiz: dibuja assets/imagenes/Fondo.jpg y ubica cada componente en una
 * zona expresada en pixeles de la imagen original (2752 x 1536).
 *
 * Como las zonas se escalan junto con la imagen, el tablero y el HUD siguen
 * encajando en el diseno del fondo aunque se cambie el tamano de la ventana.
 * La imagen conserva su proporcion: si la ventana tiene otra, quedan bordes
 * oscuros en lugar de deformarla.
 */
public class PanelFondo extends JPanel {

    public static final int ANCHO_REF = 2752;
    public static final int ALTO_REF = 1536;

    private static final String ARCHIVO = "assets/imagenes/Fondo.jpg";
    private static final int ANCHO_INICIAL = 1440;

    private final Image fondo = cargar();
    private final List<Component> componentes = new ArrayList<>();
    private final List<Rectangle> zonas = new ArrayList<>();

    public PanelFondo() {
        super(null);
        setBackground(new Color(16, 12, 16));
    }

    private static Image cargar() {
        try {
            File archivo = new File(ARCHIVO);
            return archivo.exists() ? ImageIO.read(archivo) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Ubica el componente en la zona (x, y, ancho, alto) de la imagen de referencia. */
    public void agregar(Component componente, int x, int y, int ancho, int alto) {
        componentes.add(componente);
        zonas.add(new Rectangle(x, y, ancho, alto));
        add(componente);
    }

    /** Igual que agregar(), pero (cx, cy) es el centro de la zona. */
    public void agregarCentrado(Component componente, int cx, int cy, int ancho, int alto) {
        agregar(componente, cx - ancho / 2, cy - alto / 2, ancho, alto);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ANCHO_INICIAL, ANCHO_INICIAL * ALTO_REF / ANCHO_REF);
    }

    private double escala() {
        return Math.min((double) getWidth() / ANCHO_REF, (double) getHeight() / ALTO_REF);
    }

    private int origenX() {
        return (int) Math.round((getWidth() - ANCHO_REF * escala()) / 2);
    }

    private int origenY() {
        return (int) Math.round((getHeight() - ALTO_REF * escala()) / 2);
    }

    @Override
    public void doLayout() {
        double s = escala();
        int ox = origenX();
        int oy = origenY();
        for (int i = 0; i < componentes.size(); i++) {
            Rectangle z = zonas.get(i);
            componentes.get(i).setBounds(
                    ox + (int) Math.round(z.x * s),
                    oy + (int) Math.round(z.y * s),
                    (int) Math.round(z.width * s),
                    (int) Math.round(z.height * s));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double s = escala();
        g2.drawImage(fondo, origenX(), origenY(),
                (int) Math.round(ANCHO_REF * s), (int) Math.round(ALTO_REF * s), null);
    }
}
