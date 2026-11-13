package sokoban.modelo.celdas;

/**
 * Lugar donde debe terminar una caja. La condicion de victoria se evalua
 * preguntando si todas las casillas destino estan ocupadas.
 */
public class CasillaDestino extends Celda {

    public CasillaDestino(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public boolean esTransitable() {
        return true;
    }
}
