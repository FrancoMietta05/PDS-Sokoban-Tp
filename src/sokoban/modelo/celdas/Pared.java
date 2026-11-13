package sokoban.modelo.celdas;

/** Limite fijo del almacen. Nunca es transitable por las reglas normales. */
public class Pared extends Celda {

    public Pared(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public boolean esTransitable() {
        return false;
    }
}
