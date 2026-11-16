package sokoban.modelo.memento;

import java.util.ArrayList;
import java.util.List;

import sokoban.powerups.EstadoPowerUp;

/**
 * Foto del estado de la partida. Rol Memento del patron: datos, sin
 * comportamiento de juego.
 *
 * Solo Juego (el originator) sabe armarlo e interpretarlo.
 * HistorialMovimientos (el caretaker) lo apila y lo desapila sin abrirlo.
 *
 * Guarda todo lo que un undo tiene que poder reponer:
 *   - posicion del jugador y sus contadores
 *   - posicion, resistencia y rotura de cada caja
 *   - que muros estaban abiertos y que cerrojos activados
 *   - que power-ups del nivel ya estaban recogidos
 *   - la cadena de power-ups activa sobre el jugador
 *
 * Las tres ultimas listas son adiciones respecto del diagrama: sin ellas el
 * undo reponia las cajas pero dejaba el resto del tablero adelantado.
 *
 * La posicion del jugador se guarda como dos int (fila y columna) y no como
 * java.awt.Point, asi el modelo no importa nada de AWT (SOK-15).
 */
public final class Memento {

    private final int filaJugador;
    private final int columnaJugador;
    private final int movimientos;
    private final int empujes;
    private final List<EstadoCaja> estadoCajas;
    private final List<Boolean> murosAbiertos;
    private final List<Boolean> cerrojosActivados;
    private final List<Boolean> powerUpsRecogidos;
    private final List<EstadoPowerUp> powerUpsActivos;

    public Memento(int filaJugador,
                   int columnaJugador,
                   int movimientos,
                   int empujes,
                   List<EstadoCaja> estadoCajas,
                   List<Boolean> murosAbiertos,
                   List<Boolean> cerrojosActivados,
                   List<Boolean> powerUpsRecogidos,
                   List<EstadoPowerUp> powerUpsActivos) {
        this.filaJugador = filaJugador;
        this.columnaJugador = columnaJugador;
        this.movimientos = movimientos;
        this.empujes = empujes;
        this.estadoCajas = List.copyOf(estadoCajas);
        this.murosAbiertos = List.copyOf(murosAbiertos);
        this.cerrojosActivados = List.copyOf(cerrojosActivados);
        this.powerUpsRecogidos = List.copyOf(powerUpsRecogidos);
        this.powerUpsActivos = new ArrayList<>(powerUpsActivos);
    }

    public int getFilaJugador() {
        return filaJugador;
    }

    public int getColumnaJugador() {
        return columnaJugador;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public int getEmpujes() {
        return empujes;
    }

    public List<EstadoCaja> getEstadoCajas() {
        return estadoCajas;
    }

    public List<Boolean> getMurosAbiertos() {
        return murosAbiertos;
    }

    public List<Boolean> getCerrojosActivados() {
        return cerrojosActivados;
    }

    public List<Boolean> getPowerUpsRecogidos() {
        return powerUpsRecogidos;
    }

    public List<EstadoPowerUp> getPowerUpsActivos() {
        return List.copyOf(powerUpsActivos);
    }

    /** Estado de una caja dentro de la foto. */
    public static final class EstadoCaja {

        private final int fila;
        private final int columna;
        private final int resistencia;
        private final boolean rota;

        public EstadoCaja(int fila, int columna, int resistencia, boolean rota) {
            this.fila = fila;
            this.columna = columna;
            this.resistencia = resistencia;
            this.rota = rota;
        }

        public int getFila() {
            return fila;
        }

        public int getColumna() {
            return columna;
        }

        public int getResistencia() {
            return resistencia;
        }

        public boolean estaRota() {
            return rota;
        }
    }
}
