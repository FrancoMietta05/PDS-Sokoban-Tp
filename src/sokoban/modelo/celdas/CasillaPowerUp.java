package sokoban.modelo.celdas;

import sokoban.modelo.TipoPowerUp;

/**
 * Power-up tirado en el nivel.
 *
 * Es una subclase de Celda y no un atributo opcional de Celda (decision 5 de
 * la bitacora): asi entra sin modificar la clase base ni el codigo de
 * movimiento, igual que entro el terreno resbaladizo.
 *
 * recoger() devuelve el TipoPowerUp y se marca como recogido. La celda no
 * sabe que los power-ups se implementan con decoradores: traducir el tipo al
 * decorador concreto es tarea de GestorPowerUps.
 */
public class CasillaPowerUp extends Celda {

    private final TipoPowerUp tipo;
    private boolean recogido;

    public CasillaPowerUp(int fila, int columna, TipoPowerUp tipo) {
        super(fila, columna);
        this.tipo = tipo;
    }

    /**
     * @return el tipo de power-up, o null si ya habia sido recogido.
     */
    public TipoPowerUp recoger() {
        if (recogido) {
            return null;
        }
        recogido = true;
        return tipo;
    }

    public TipoPowerUp getTipo() {
        return tipo;
    }

    public boolean estaRecogido() {
        return recogido;
    }

    /** Usado al restaurar un memento. */
    public void restaurarEstado(boolean recogido) {
        this.recogido = recogido;
    }

    @Override
    public boolean esTransitable() {
        return true;
    }
}
