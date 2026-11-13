package sokoban.modelo.entidades;

/**
 * Todo lo que ocupa una celda y puede cambiar de celda: el jugador y las cajas.
 *
 * DESVIACION RESPECTO DEL DIAGRAMA (pendiente 3 de la bitacora): en el diagrama
 * Caja no heredaba de EntidadMovil y quedaba como jerarquia suelta. Al codear
 * eso obligaba a duplicar fila, columna y mover() en Caja. Se resolvio haciendo
 * que Caja tambien herede de aca: EntidadMovil es la abstraccion de "cosa que
 * ocupa una celda del tablero y se desplaza", y tanto el jugador como las cajas
 * lo son.
 */
public abstract class EntidadMovil {

    private int fila;
    private int columna;

    protected EntidadMovil(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    /** Reubica la entidad. El tablero ya valido que el destino sea legal. */
    public void mover(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }
}
