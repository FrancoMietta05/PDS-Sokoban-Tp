package sokoban.vista;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Pantalla que aparece al completar el ultimo nivel. Muestra
 * assets/imagenes/Final.png, que ya trae dibujados el mensaje y los dos
 * botones: esta clase solo sabe donde estan y avisa cual eligio el usuario.
 *
 * Es una vista pasiva: no conoce el juego ni la ventana. Quien la muestra le
 * pasa que hacer al volver a iniciar y al cerrar (ver PanelCartel).
 */
public class PanelFinal extends PanelCartel {

    private static final String ARCHIVO = "assets/imagenes/Final.png";
    /** Final.png ya esta recortado al cartel: se usa entero. */
    private static final Rectangle RECORTE = new Rectangle(0, 0, 1849, 1486);

    private static final Rectangle BOTON_VOLVER_A_INICIAR = new Rectangle(290, 1180, 650, 290);
    private static final Rectangle BOTON_CERRAR = new Rectangle(975, 1180, 595, 290);
    private static final int VOLVER_A_INICIAR = 0;
    private static final int CERRAR = 1;

    private Runnable alVolverAIniciar = () -> { };
    private Runnable alCerrar = () -> { };

    public PanelFinal() {
        super(ARCHIVO, RECORTE, 0.42, 0.516, BOTON_VOLVER_A_INICIAR, BOTON_CERRAR);
        atajo(KeyEvent.VK_ENTER, this::volverAIniciar);
    }

    /** Muestra la pantalla final. */
    public void mostrar(Runnable alVolverAIniciar, Runnable alCerrar) {
        this.alVolverAIniciar = alVolverAIniciar;
        this.alCerrar = alCerrar;
        abrir();
    }

    @Override
    protected void alPresionar(int boton) {
        if (boton == VOLVER_A_INICIAR) {
            volverAIniciar();
        } else if (boton == CERRAR) {
            cerrar();
            alCerrar.run();
        }
    }

    private void volverAIniciar() {
        cerrar();
        alVolverAIniciar.run();
    }

    @Override
    protected void dibujarContenido(Graphics2D g2) {
        // El texto y los botones ya vienen en la imagen.
    }
}
