package sokoban.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;
import sokoban.modelo.Tablero;
import sokoban.modelo.celdas.CasillaCerrojo;
import sokoban.modelo.celdas.CasillaDestino;
import sokoban.modelo.celdas.CasillaPowerUp;
import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.Muro;
import sokoban.modelo.celdas.Pared;
import sokoban.modelo.celdas.TerrenoResbaladizo;
import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaFragil;
import sokoban.modelo.entidades.CajaLlave;

/**
 * Vista del tablero. Lee el modelo para dibujarlo y no lo modifica nunca.
 *
 * Es observador: se redibuja cuando llega cualquier evento, en lugar de que el
 * controlador tenga que acordarse de pedirle un repaint. Asi el modelo sigue
 * sin conocer a la vista.
 *
 * Si existen las imagenes en assets/imagenes las usa; si no, dibuja figuras de
 * color. Eso permite tener el juego andando antes de incorporar los assets de
 * dominio publico que pide el enunciado.
 */
public class PanelTablero extends JPanel implements Observador {

    private static final int LADO_CELDA = 64;
    private static final String CARPETA_IMAGENES = "assets/imagenes/";

    private final Juego juego;
    private final Map<String, Image> imagenes = new HashMap<>();

    public PanelTablero(Juego juego) {
        this.juego = juego;
        setBackground(new Color(28, 28, 34));
        setFocusable(true);
    }

    @Override
    public void actualizar(EventoJuego e) {
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Tablero tablero = juego.getTableroActual();
        if (tablero == null) {
            return new Dimension(640, 480);
        }
        return new Dimension(tablero.getColumnas() * LADO_CELDA, tablero.getFilas() * LADO_CELDA);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Tablero tablero = juego.getTableroActual();
        if (tablero == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int f = 0; f < tablero.getFilas(); f++) {
            for (int c = 0; c < tablero.getColumnas(); c++) {
                dibujarCelda(g2, tablero.obtenerCelda(f, c), f, c);
            }
        }
        for (Caja caja : tablero.getCajas()) {
            if (!caja.estaRota()) {
                dibujarCaja(g2, tablero, caja);
            }
        }
        dibujarJugador(g2, tablero);
    }

    private void dibujarCelda(Graphics2D g2, Celda celda, int fila, int columna) {
        int x = columna * LADO_CELDA;
        int y = fila * LADO_CELDA;

        // El cerrojo y los power-ups son marcas que van encima del piso, no
        // pisos en si: se dibuja primero la base y despues la marca, que tiene
        // fondo transparente. Asi el sprite de la marca sirve sobre cualquier
        // piso y no hay que tener una version por combinacion.
        String clave = claveDeCelda(celda);
        if (celda instanceof CasillaCerrojo || celda instanceof CasillaPowerUp) {
            dibujarImagen(g2, "piso", x, y);
        }
        if (dibujarImagen(g2, clave, x, y)) {
            return;
        }

        if (celda instanceof Pared) {
            g2.setColor(new Color(90, 78, 68));
            g2.fillRect(x, y, LADO_CELDA, LADO_CELDA);
            g2.setColor(new Color(70, 60, 52));
            g2.drawRect(x, y, LADO_CELDA - 1, LADO_CELDA - 1);
        } else if (celda instanceof Muro) {
            boolean abierto = ((Muro) celda).estaAbierto();
            g2.setColor(abierto ? new Color(60, 60, 70) : new Color(150, 60, 60));
            g2.fillRect(x, y, LADO_CELDA, LADO_CELDA);
            g2.setColor(new Color(220, 200, 120));
            g2.drawRect(x + 6, y + 6, LADO_CELDA - 13, LADO_CELDA - 13);
        } else if (celda instanceof CasillaDestino) {
            pisoBase(g2, x, y);
            g2.setColor(new Color(230, 180, 60));
            g2.drawOval(x + 12, y + 12, LADO_CELDA - 24, LADO_CELDA - 24);
        } else if (celda instanceof TerrenoResbaladizo) {
            g2.setColor(new Color(120, 190, 220));
            g2.fillRect(x, y, LADO_CELDA, LADO_CELDA);
            g2.setColor(new Color(200, 235, 245));
            g2.drawLine(x + 8, y + 12, x + LADO_CELDA - 8, y + 20);
            g2.drawLine(x + 8, y + 28, x + LADO_CELDA - 8, y + 36);
        } else if (celda instanceof CasillaCerrojo) {
            pisoBase(g2, x, y);
            CasillaCerrojo cerrojo = (CasillaCerrojo) celda;
            g2.setColor(cerrojo.estaActivado() ? new Color(110, 200, 130) : new Color(200, 170, 80));
            g2.fillRoundRect(x + 10, y + 10, LADO_CELDA - 20, LADO_CELDA - 20, 8, 8);
            g2.setColor(new Color(40, 40, 45));
            g2.drawString("C", x + LADO_CELDA / 2 - 4, y + LADO_CELDA / 2 + 5);
        } else if (celda instanceof CasillaPowerUp) {
            pisoBase(g2, x, y);
            CasillaPowerUp casilla = (CasillaPowerUp) celda;
            if (!casilla.estaRecogido()) {
                g2.setColor(new Color(140, 110, 220));
                g2.fillOval(x + 10, y + 10, LADO_CELDA - 20, LADO_CELDA - 20);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString(String.valueOf(casilla.getTipo().getNombre().charAt(0)),
                        x + LADO_CELDA / 2 - 5, y + LADO_CELDA / 2 + 5);
            }
        } else {
            pisoBase(g2, x, y);
        }
    }

    private void pisoBase(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(48, 48, 56));
        g2.fillRect(x, y, LADO_CELDA, LADO_CELDA);
        g2.setColor(new Color(40, 40, 48));
        g2.drawRect(x, y, LADO_CELDA - 1, LADO_CELDA - 1);
    }

    private void dibujarCaja(Graphics2D g2, Tablero tablero, Caja caja) {
        int x = caja.getColumna() * LADO_CELDA;
        int y = caja.getFila() * LADO_CELDA;
        boolean enDestino =
                tablero.obtenerCelda(caja.getFila(), caja.getColumna()) instanceof CasillaDestino;

        String clave = claveDeCaja(caja) + (enDestino ? "_ok" : "");
        if (dibujarImagen(g2, clave, x, y)) {
            return;
        }

        Color color;
        switch (caja.getSimbolo()) {
            case 'F':
                color = new Color(200, 120, 90);
                break;
            case 'L':
                color = new Color(210, 190, 90);
                break;
            default:
                color = new Color(170, 130, 85);
        }
        if (enDestino) {
            color = color.brighter();
        }
        g2.setColor(color);
        g2.fillRoundRect(x + 5, y + 5, LADO_CELDA - 10, LADO_CELDA - 10, 8, 8);
        g2.setColor(color.darker().darker());
        g2.drawRoundRect(x + 5, y + 5, LADO_CELDA - 11, LADO_CELDA - 11, 8, 8);
        g2.drawLine(x + 5, y + 5, x + LADO_CELDA - 6, y + LADO_CELDA - 6);
        g2.drawLine(x + LADO_CELDA - 6, y + 5, x + 5, y + LADO_CELDA - 6);

        if (caja.getResistencia() > 0) {
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            g2.drawString(String.valueOf(caja.getResistencia()), x + LADO_CELDA - 16, y + 18);
        }
    }

    private void dibujarJugador(Graphics2D g2, Tablero tablero) {
        if (juego.getJugador() == null) {
            return;
        }
        int x = juego.getJugador().getColumna() * LADO_CELDA;
        int y = juego.getJugador().getFila() * LADO_CELDA;
        if (dibujarImagen(g2, "jugador", x, y)) {
            return;
        }
        g2.setColor(new Color(90, 180, 230));
        g2.fillOval(x + 10, y + 8, LADO_CELDA - 20, LADO_CELDA - 20);
        g2.setColor(new Color(40, 90, 130));
        g2.fillRect(x + 16, y + LADO_CELDA - 20, LADO_CELDA - 32, 12);
    }

    /** Devuelve true si habia una imagen para esa clave y se dibujo. */
    private boolean dibujarImagen(Graphics2D g2, String clave, int x, int y) {
        Image imagen = cargarImagen(clave);
        if (imagen == null) {
            return false;
        }
        g2.drawImage(imagen, x, y, LADO_CELDA, LADO_CELDA, null);
        return true;
    }

    private Image cargarImagen(String clave) {
        if (imagenes.containsKey(clave)) {
            return imagenes.get(clave);
        }
        Image imagen = null;
        File archivo = new File(CARPETA_IMAGENES + clave + ".png");
        if (archivo.exists()) {
            try {
                imagen = ImageIO.read(archivo);
            } catch (Exception e) {
                imagen = null;
            }
        }
        imagenes.put(clave, imagen);
        return imagen;
    }

    /**
     * Nombre de archivo del sprite de una caja.
     *
     * La traduccion vive aca y no en el modelo a proposito. Antes la clave se
     * armaba con Caja.getSimbolo(), que es el caracter del archivo .txt: eso
     * ataba el nombre de un asset al formato de los niveles (y hacia que la
     * caja normal buscara un archivo llamado "caja_$.png"). Son dos cosas que
     * cambian por motivos distintos, asi que cada una vive donde corresponde.
     */
    private String claveDeCaja(Caja caja) {
        if (caja instanceof CajaFragil) {
            return "caja_fragil";
        }
        if (caja instanceof CajaLlave) {
            return "caja_llave";
        }
        return "caja_normal";
    }

    private String claveDeCelda(Celda celda) {
        if (celda instanceof Pared) {
            return "pared";
        }
        if (celda instanceof Muro) {
            return ((Muro) celda).estaAbierto() ? "muro_abierto" : "muro_cerrado";
        }
        if (celda instanceof CasillaDestino) {
            return "destino";
        }
        if (celda instanceof TerrenoResbaladizo) {
            return "resbaladizo";
        }
        if (celda instanceof CasillaCerrojo) {
            return ((CasillaCerrojo) celda).estaActivado() ? "cerrojo_abierto" : "cerrojo";
        }
        if (celda instanceof CasillaPowerUp) {
            CasillaPowerUp casilla = (CasillaPowerUp) celda;
            return casilla.estaRecogido() ? "piso" : "powerup_" + casilla.getTipo().name().toLowerCase();
        }
        return "piso";
    }
}
