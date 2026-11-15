package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaNormal;

/** Creador concreto del Factory Method: fabrica CajaNormal. */
public class FabricaCajaNormal implements FabricaCaja {

    @Override
    public Caja crear(int fila, int columna) {
        return new CajaNormal(fila, columna);
    }
}
