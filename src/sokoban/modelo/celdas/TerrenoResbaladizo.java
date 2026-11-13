package sokoban.modelo.celdas;

/**
 * Piso sobre el que las cosas se deslizan: lo que entra en esta celda sigue
 * avanzando en la misma direccion hasta chocar con un objeto o hasta pisar
 * un piso que no resbale.
 *
 * El deslizamiento lo resuelve Tablero, porque es el unico que conoce las
 * celdas vecinas y las cajas; la celda solo se declara resbaladiza.
 */
public class TerrenoResbaladizo extends Celda {

    public TerrenoResbaladizo(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public boolean esTransitable() {
        return true;
    }
}
