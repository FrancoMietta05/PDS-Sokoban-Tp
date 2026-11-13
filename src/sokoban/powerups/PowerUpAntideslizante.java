package sokoban.powerups;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;

/** El jugador deja de deslizarse al pisar terreno resbaladizo. */
public class PowerUpAntideslizante extends PowerUpDecorator {

    public static final int DURACION_POR_DEFECTO = 20;

    public PowerUpAntideslizante(IJugador jugador) {
        this(jugador, DURACION_POR_DEFECTO);
    }

    public PowerUpAntideslizante(IJugador jugador, int duracion) {
        super(jugador, duracion, ILIMITADO);
    }

    @Override
    public boolean resisteDeslizamiento() {
        return true;
    }

    @Override
    public TipoPowerUp getTipo() {
        return TipoPowerUp.ANTIDESLIZANTE;
    }
}
