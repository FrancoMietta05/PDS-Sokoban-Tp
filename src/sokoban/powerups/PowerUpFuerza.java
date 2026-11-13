package sokoban.powerups;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;

/** Permite empujar dos cajas en linea en lugar de una. */
public class PowerUpFuerza extends PowerUpDecorator {

    public static final int DURACION_POR_DEFECTO = 15;

    public PowerUpFuerza(IJugador jugador) {
        this(jugador, DURACION_POR_DEFECTO);
    }

    public PowerUpFuerza(IJugador jugador, int duracion) {
        super(jugador, duracion, ILIMITADO);
    }

    @Override
    public int cajasEmpujables() {
        return 2;
    }

    @Override
    public TipoPowerUp getTipo() {
        return TipoPowerUp.FUERZA;
    }
}
