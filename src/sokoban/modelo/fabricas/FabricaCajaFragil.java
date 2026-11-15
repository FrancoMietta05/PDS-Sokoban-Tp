package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaFragil;

/** Creador concreto del Factory Method: fabrica CajaFragil. */
public class FabricaCajaFragil implements FabricaCaja {

    @Override
    public Caja crear(int fila, int columna) {
        return new CajaFragil(fila, columna);
    }
}
