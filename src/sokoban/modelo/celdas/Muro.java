package sokoban.modelo.celdas;

/**
 * Pared que se puede abrir y cerrar. Es transitable solo cuando esta abierta.
 *
 * Los muros los abre una CasillaCerrojo al ser activada por una CajaLlave.
 */
public class Muro extends Celda {

    private boolean abierto;

    public Muro(int fila, int columna, boolean abierto) {
        super(fila, columna);
        this.abierto = abierto;
    }

    public void abrir() {
        this.abierto = true;
    }

    public void cerrar() {
        this.abierto = false;
    }

    public boolean estaAbierto() {
        return abierto;
    }

    /** Usado al restaurar un memento: repone el estado sin disparar eventos. */
    public void restaurarEstado(boolean abierto) {
        this.abierto = abierto;
    }

    @Override
    public boolean esTransitable() {
        return abierto;
    }
}
