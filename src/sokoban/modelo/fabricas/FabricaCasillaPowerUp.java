package sokoban.modelo.fabricas;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.celdas.CasillaPowerUp;
import sokoban.modelo.celdas.Celda;

/**
 * Creador concreto del Factory Method: fabrica CasillaPowerUp. Cada instancia
 * produce casillas de un unico TipoPowerUp.
 */
public class FabricaCasillaPowerUp implements FabricaCelda {

    private final TipoPowerUp tipo;

    public FabricaCasillaPowerUp(TipoPowerUp tipo) {
        this.tipo = tipo;
    }

    @Override
    public Celda crear(int fila, int columna) {
        return new CasillaPowerUp(fila, columna, tipo);
    }
}
