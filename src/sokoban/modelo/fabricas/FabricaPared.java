package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.Pared;

/** Creador concreto del Factory Method: fabrica Pared. */
public class FabricaPared implements FabricaCelda {

    @Override
    public Celda crear(int fila, int columna) {
        return new Pared(fila, columna);
    }
}
