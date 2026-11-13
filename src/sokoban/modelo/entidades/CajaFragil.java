package sokoban.modelo.entidades;

/**
 * Caja que se banca una cantidad limitada de empujes. Cada empuje le baja la
 * resistencia y al llegar a cero se rompe.
 *
 * Un jugador con Manos de Acero la rompe de un solo empuje.
 */
public class CajaFragil extends Caja {

    public static final int RESISTENCIA_POR_DEFECTO = 3;

    private int resistencia;

    public CajaFragil(int fila, int columna) {
        this(fila, columna, RESISTENCIA_POR_DEFECTO);
    }

    public CajaFragil(int fila, int columna, int resistencia) {
        super(fila, columna);
        this.resistencia = resistencia;
    }

    @Override
    public void registrarEmpuje(boolean rompeFragiles) {
        resistencia -= rompeFragiles ? resistencia : 1;
        if (resistencia <= 0) {
            romperse();
        }
    }

    public void romperse() {
        resistencia = 0;
        marcarRota();
    }

    @Override
    public int getResistencia() {
        return resistencia;
    }

    @Override
    public void restaurarEstado(int fila, int columna, int resistencia, boolean rota) {
        super.restaurarEstado(fila, columna, resistencia, rota);
        this.resistencia = resistencia;
    }

    @Override
    public char getSimbolo() {
        return 'F';
    }
}
