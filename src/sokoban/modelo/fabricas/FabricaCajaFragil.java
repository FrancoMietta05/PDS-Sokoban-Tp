package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaFragil;

/**
 * Creador concreto del Factory Method: fabrica CajaFragil.
 *
 * Cada instancia de la fabrica sabe con que resistencia arranca la caja, asi
 * DirectorNivel registra una fabrica por caracter ('F', 'G', 'H') sin que
 * CajaFragil ni el tablero sepan nada del formato del .txt (SOK-12).
 */
public class FabricaCajaFragil implements FabricaCaja {

    private final int resistencia;

    public FabricaCajaFragil() {
        this(CajaFragil.RESISTENCIA_POR_DEFECTO);
    }

    public FabricaCajaFragil(int resistencia) {
        if (resistencia <= 0) {
            throw new IllegalArgumentException("La resistencia tiene que ser positiva");
        }
        this.resistencia = resistencia;
    }

    @Override
    public Caja crear(int fila, int columna) {
        return new CajaFragil(fila, columna, resistencia);
    }
}
