package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.CasillaDestino;

/** Creador concreto del Factory Method: fabrica CasillaDestino. */
public class FabricaDestino implements FabricaCelda {

    @Override
    public Celda crear(int fila, int columna) {
        return new CasillaDestino(fila, columna);
    }
}
