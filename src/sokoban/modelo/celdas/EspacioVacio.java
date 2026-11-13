package sokoban.modelo.celdas;

/** Piso libre del almacen. */
public class EspacioVacio extends Celda {

    public EspacioVacio(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public boolean esTransitable() {
        return true;
    }
}
