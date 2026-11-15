package sokoban.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;

/**
 * Pantalla de resumen que aparece al completar un nivel: dibuja las
 * estadisticas sobre assets/imagenes/Template resumen de nivel.png.
 *
 * El template es una imagen de 2752 x 1536 con el cartel en el medio; se
 * recorta a la zona del cartel y todo se ubica en pixeles de ese recorte, de
 * modo que escala junto con la ventana (ver PanelCartel).
 *
 * Es una vista pasiva: no conoce el modelo. Recibe los numeros ya calculados y
 * avisa con dos Runnable que el usuario eligio continuar o reiniciar.
 */
public class PanelResumen extends PanelCartel {

    private static final String ARCHIVO = "assets/imagenes/Template resumen de nivel.png";
    /** Zona del cartel dentro del template (con un pequeno margen). */
    private static final Rectangle RECORTE = new Rectangle(770, 355, 1216, 995);

    private static final Rectangle BOTON_CONTINUAR = new Rectangle(366, 817, 482, 128);
    private static final Rectangle BOTON_REINICIAR = new Rectangle(965, 825, 111, 109);
    private static final int CONTINUAR = 0;
    private static final int REINICIAR = 1;

    private static final Rectangle TITULO = new Rectangle(188, 32, 834, 116);

    private static final int TEXTO_X = 238;
    private static final int TEXTO_ANCHO = 650;
    private static final int[] LINEAS_Y = {266, 360, 455, 549, 645};
    private static final float TAMANO_TEXTO = 44f;

    private static final int MAX_ESTRELLAS = 5;

    private String[] lineas = new String[0];
    private int logros;
    private int totalLogros;
    private Runnable alContinuar = () -> { };
    private Runnable alReiniciar = () -> { };

    public PanelResumen() {
        super(ARCHIVO, RECORTE, 0.42, 0.516, BOTON_CONTINUAR, BOTON_REINICIAR);
        atajo(KeyEvent.VK_ENTER, this::continuar);
        atajo(KeyEvent.VK_SPACE, this::continuar);
        atajo(KeyEvent.VK_R, this::reiniciar);
    }

    /** Muestra el resumen con los datos del nivel recien completado. */
    public void mostrar(int movimientos, int empujes, int deshacer, int puntaje,
            int logros, int totalLogros, Runnable alContinuar, Runnable alReiniciar) {
        this.lineas = new String[] {
                "MOVIMIENTOS: " + movimientos,
                "EMPUJES: " + empujes,
                "DESHACER: " + deshacer,
                "PUNTAJE: " + puntaje,
                "LOGROS: " + logros + " de " + totalLogros
        };
        this.logros = logros;
        this.totalLogros = totalLogros;
        this.alContinuar = alContinuar;
        this.alReiniciar = alReiniciar;
        abrir();
    }

    @Override
    protected void alPresionar(int boton) {
        if (boton == CONTINUAR) {
            continuar();
        } else if (boton == REINICIAR) {
            reiniciar();
        }
    }

    private void continuar() {
        cerrar();
        alContinuar.run();
    }

    private void reiniciar() {
        cerrar();
        alReiniciar.run();
    }

    // ------------------------------------------------------------------
    // Dibujo
    // ------------------------------------------------------------------

    @Override
    protected void dibujarContenido(Graphics2D g2) {
        Estilo.textoCentrado(g2, "¡NIVEL COMPLETADO!", TITULO.x + 30, TITULO.width - 60,
                TITULO.y + 76, 54f, Estilo.ETIQUETA);

        for (int i = 0; i < lineas.length; i++) {
            Estilo.texto(g2, lineas[i], TEXTO_X, LINEAS_Y[i] + 16, TAMANO_TEXTO, TEXTO_ANCHO, Estilo.TEXTO);
        }
        dibujarEstrellas(g2);
        dibujarBotonContinuar(g2);
        dibujarBotonReiniciar(g2);
    }

    private void dibujarEstrellas(Graphics2D g2) {
        int paso = 46;
        int ancho = MAX_ESTRELLAS * paso;
        int x0 = 846 - ancho + paso / 2;
        int cantidad = totalLogros <= 0 ? 0 : Math.round((float) logros * MAX_ESTRELLAS / totalLogros);
        for (int i = 0; i < MAX_ESTRELLAS; i++) {
            boolean activa = i < cantidad;
            dibujarEstrella(g2, x0 + i * paso, 718, 20, activa);
        }
    }

    private void dibujarEstrella(Graphics2D g2, int cx, int cy, int radio, boolean activa) {
        Polygon p = new Polygon();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radio : radio * 0.45;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            p.addPoint((int) Math.round(cx + r * Math.cos(a)), (int) Math.round(cy + r * Math.sin(a)));
        }
        g2.setColor(activa ? new Color(255, 200, 40) : new Color(70, 52, 44));
        g2.fillPolygon(p);
        g2.setColor(activa ? new Color(160, 96, 16) : new Color(40, 28, 26));
        g2.setStroke(new BasicStroke(3f));
        g2.drawPolygon(p);
    }

    private void dibujarBotonContinuar(Graphics2D g2) {
        Rectangle b = BOTON_CONTINUAR;
        Estilo.textoCentrado(g2, "CONTINUAR", b.x + 30, b.width - 60, b.y + b.height / 2 + 17, 46f, Estilo.TEXTO);
    }

    private void dibujarBotonReiniciar(Graphics2D g2) {
        Rectangle b = BOTON_REINICIAR;
        int cx = b.x + b.width / 2;
        int cy = b.y + b.height / 2;
        int r = 28;
        g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Estilo.SOMBRA);
        g2.draw(new Arc2D.Double(cx - r + 3, cy - r + 3, r * 2, r * 2, 40, 280, Arc2D.OPEN));
        g2.setColor(Estilo.TEXTO);
        g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 40, 280, Arc2D.OPEN));
        // punta de flecha en el extremo superior derecho del arco
        double a = Math.toRadians(40);
        int px = (int) Math.round(cx + r * Math.cos(a));
        int py = (int) Math.round(cy - r * Math.sin(a));
        Polygon flecha = new Polygon(
                new int[] {px - 6, px + 20, px + 8},
                new int[] {py - 22, py - 4, py + 16}, 3);
        g2.fillPolygon(flecha);
    }
}
