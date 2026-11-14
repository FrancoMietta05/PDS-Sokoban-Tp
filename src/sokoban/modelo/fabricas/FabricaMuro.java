package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.Muro;

/**
 * Creador concreto del Factory Method: fabrica Muro. Se configura con el
 * estado inicial porque el formato de nivel distingue el muro cerrado ('M')
 * del abierto ('m').
 */
public class FabricaMuro implements FabricaCelda {

    private final boolean abierto;

    public FabricaMuro(boolean abierto) {
        this.abierto = abierto;
    }

    @Override
    public Celda crear(int fila, int columna) {
        return new Muro(fila, columna, abierto);
    }
}
