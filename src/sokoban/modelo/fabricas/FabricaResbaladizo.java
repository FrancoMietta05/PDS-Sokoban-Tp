package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.TerrenoResbaladizo;

/** Creador concreto del Factory Method: fabrica TerrenoResbaladizo. */
public class FabricaResbaladizo implements FabricaCelda {

    @Override
    public Celda crear(int fila, int columna) {
        return new TerrenoResbaladizo(fila, columna);
    }
}
