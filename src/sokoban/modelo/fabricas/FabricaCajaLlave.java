package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaLlave;

/** Creador concreto del Factory Method: fabrica CajaLlave. */
public class FabricaCajaLlave implements FabricaCaja {

    @Override
    public Caja crear(int fila, int columna) {
        return new CajaLlave(fila, columna);
    }
}
