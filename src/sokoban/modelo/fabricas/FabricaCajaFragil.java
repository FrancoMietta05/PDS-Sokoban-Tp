package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaFragil;

/**
 * Creador concreto del Factory Method: fabrica CajaFragil.
 *
 * La resistencia inicial es un parametro de la fabrica, no de la caja que se
 * pide: asi el director registra una instancia por cada resistencia que el
 * formato de nivel declare, sin tocar CajaFragil ni Tablero.
 */
public class FabricaCajaFragil implements FabricaCaja {

    private final int resistencia;

    public FabricaCajaFragil() {
        this(CajaFragil.RESISTENCIA_POR_DEFECTO);
    }

    public FabricaCajaFragil(int resistencia) {
        if (resistencia <= 0) {
            throw new IllegalArgumentException("La resistencia debe ser positiva: " + resistencia);
        }
        this.resistencia = resistencia;
    }

    @Override
    public Caja crear(int fila, int columna) {
        return new CajaFragil(fila, columna, resistencia);
    }
}
