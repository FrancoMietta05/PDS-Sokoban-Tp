package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.CasillaCerrojo;

/** Creador concreto del Factory Method: fabrica CasillaCerrojo. */
public class FabricaCerrojo implements FabricaCelda {

    @Override
    public Celda crear(int fila, int columna) {
        return new CasillaCerrojo(fila, columna);
    }
}
