package sokoban.modelo.celdas;

/**
 * Casilla del tablero. Raiz de la jerarquia que resuelve por polimorfismo
 * el comportamiento de cada tipo de piso.
 *
 * La posicion es inmutable: una celda no se mueve, el tablero la ubica al
 * construirla.
 */
public abstract class Celda {

    private final int fila;
    private final int columna;

    protected Celda(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    /**
     * Si una entidad puede pararse en esta celda por las reglas normales.
     * Un power-up puede pasar por encima de esta respuesta (ver
     * IJugador.puedeAtravesar), pero las cajas nunca.
     */
    public abstract boolean esTransitable();

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }
}
