package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.EspacioVacio;

/** Creador concreto del Factory Method: fabrica EspacioVacio. */
public class FabricaEspacioVacio implements FabricaCelda {

    @Override
    public Celda crear(int fila, int columna) {
        return new EspacioVacio(fila, columna);
    }
}
