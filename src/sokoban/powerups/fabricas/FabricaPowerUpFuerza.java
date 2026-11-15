package sokoban.powerups.fabricas;

import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.PowerUpDecorator;
import sokoban.powerups.PowerUpFuerza;

/** Creador concreto del Factory Method: fabrica PowerUpFuerza. */
public class FabricaPowerUpFuerza implements FabricaPowerUp {

    @Override
    public PowerUpDecorator crear(IJugador envuelto) {
        return new PowerUpFuerza(envuelto);
    }

    @Override
    public PowerUpDecorator recrear(IJugador envuelto, int duracion, int usosRestantes) {
        return new PowerUpFuerza(envuelto, duracion);
    }
}
