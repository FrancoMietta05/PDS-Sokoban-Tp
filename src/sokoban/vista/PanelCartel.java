package sokoban.vista;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Base de las pantallas que se muestran como un cartel sobre el juego
 * oscurecido (resumen de nivel, juego completado). Es un glass pane de la
 * ventana.
 *
 * Concentra lo que esas pantallas tienen en comun: cargar la imagen del
 * cartel, centrarla y escalarla con la ventana, detectar el hover y el click
 * sobre sus botones y atajos de teclado. Las subclases solo declaran donde
 * estan los botones (en pixeles de la imagen), que hace cada uno y que se
 * dibuja encima del cartel.
 *
 * Es vista pasiva: no conoce el modelo ni a la ventana. Quien la usa le pasa
 * los Runnable que quiere ejecutar.
 */
public abstract class PanelCartel extends JPanel {

    private static final Color VELO = new Color(0, 0, 0, 170);
    private static final Color MARRON_BOTON = new Color(119, 70, 38);

    private final BufferedImage cartel;
    private final Rectangle recorte;
    private final double fraccionAncho;
    private final double fraccionAlto;
    private final Rectangle[] botones;
    private int hover = -1;

    /**
     * @param archivo       imagen del cartel
     * @param recorte       zona de la imagen que es el cartel, en sus pixeles;
     *                      tambien define las proporciones del cartel
     * @param fraccionAncho maximo del ancho de la ventana que ocupa el cartel
     * @param fraccionAlto  maximo del alto de la ventana que ocupa el cartel
     * @param botones       zonas clickeables, en pixeles del recorte; su
     *                      posicion en el arreglo es el indice que recibe
     *                      alPresionar()
     */
    protected PanelCartel(String archivo, Rectangle recorte, double fraccionAncho,
            double fraccionAlto, Rectangle... botones) {
        super(null);
        this.cartel = cargar(archivo, recorte);
        this.recorte = recorte;
        this.fraccionAncho = fraccionAncho;
        this.fraccionAlto = fraccionAlto;
        this.botones = botones;
        setOpaque(false);
        setVisible(false);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int nuevo = botonEn(e.getPoint());
                if (nuevo != hover) {
                    hover = nuevo;
                    setCursor(Cursor.getPredefinedCursor(
                            nuevo >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int boton = botonEn(e.getPoint());
                if (boton >= 0) {
                    alPresionar(boton);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Que hace el boton de ese indice. */
    protected abstract void alPresionar(int boton);

    /** Lo que se dibuja encima del cartel, en pixeles del recorte. */
    protected abstract void dibujarContenido(Graphics2D g2);

    /** Muestra el cartel y le da el foco para que reciba los atajos de teclado. */
    protected void abrir() {
        hover = -1;
        setVisible(true);
        requestFocusInWindow();
        repaint();
    }

    /** Oculta el cartel. */
    protected void cerrar() {
        setVisible(false);
    }

    /** Asocia una tecla a una accion; solo actua mientras el cartel esta visible. */
    protected void atajo(int tecla, Runnable accion) {
        String nombre = "cartel-" + tecla;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla, 0), nombre);
        getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isVisible()) {
                    accion.run();
                }
            }
        });
    }

    private static BufferedImage cargar(String archivo, Rectangle recorte) {
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                return null;
            }
            BufferedImage completa = ImageIO.read(f);
            if (completa.getWidth() < recorte.x + recorte.width
                    || completa.getHeight() < recorte.y + recorte.height) {
                return completa;
            }
            return completa.getSubimage(recorte.x, recorte.y, recorte.width, recorte.height);
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Geometria: el cartel se centra y se escala segun la ventana
    // ------------------------------------------------------------------

    private double escala() {
        double s = Math.min(getWidth() * fraccionAncho / recorte.width,
                getHeight() * fraccionAlto / recorte.height);
        return Math.max(0.05, s);
    }

    private AffineTransform transformacion() {
        double s = escala();
        AffineTransform t = new AffineTransform();
        t.translate((getWidth() - recorte.width * s) / 2, (getHeight() - recorte.height * s) / 2);
        t.scale(s, s);
        return t;
    }

    private int botonEn(Point p) {
        try {
            Point2D h = transformacion().inverseTransform(p, null);
            for (int i = 0; i < botones.length; i++) {
                if (botones[i].contains(h.getX(), h.getY())) {
                    return i;
                }
            }
        } catch (Exception e) {
            // sin transformacion valida no hay boton
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Dibujo
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.setColor(VELO);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.transform(transformacion());
        if (cartel != null) {
            g2.drawImage(cartel, 0, 0, null);
        } else {
            dibujarRespaldo(g2);
        }
        dibujarContenido(g2);
        if (hover >= 0) {
            resaltar(g2, botones[hover]);
        }
        g2.dispose();
    }

    /** Si falta la imagen, la pantalla sigue siendo usable sobre un panel liso. */
    private void dibujarRespaldo(Graphics2D g2) {
        g2.setColor(Estilo.PANEL);
        g2.fillRoundRect(0, 0, recorte.width, recorte.height, 40, 40);
        g2.setColor(Estilo.BORDE);
        g2.setStroke(new BasicStroke(8f));
        g2.drawRoundRect(0, 0, recorte.width, recorte.height, 40, 40);
        g2.setColor(MARRON_BOTON);
        for (Rectangle b : botones) {
            g2.fill(b);
        }
    }

    private void resaltar(Graphics2D g2, Rectangle b) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(b.x, b.y, b.width, b.height, 16, 16);
        g2.setComposite(AlphaComposite.SrcOver);
    }
}
