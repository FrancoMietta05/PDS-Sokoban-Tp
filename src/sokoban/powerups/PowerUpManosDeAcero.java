package sokoban.powerups;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;

/** Rompe una caja fragil de un solo empuje, sin importar su resistencia. */
public class PowerUpManosDeAcero extends PowerUpDecorator {

    public static final int DURACION_POR_DEFECTO = 10;

    public PowerUpManosDeAcero(IJugador jugador) {
        this(jugador, DURACION_POR_DEFECTO);
    }

    public PowerUpManosDeAcero(IJugador jugador, int duracion) {
        super(jugador, duracion, ILIMITADO);
    }

    @Override
    public boolean rompeFragiles() {
        return true;
    }

    @Override
    public TipoPowerUp getTipo() {
        return TipoPowerUp.MANOS_DE_ACERO;
    }
}
