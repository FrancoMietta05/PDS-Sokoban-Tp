package sokoban.powerups.fabricas;

import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.PowerUpDecorator;
import sokoban.powerups.PowerUpAntideslizante;

/** Creador concreto del Factory Method: fabrica PowerUpAntideslizante. */
public class FabricaPowerUpAntideslizante implements FabricaPowerUp {

    @Override
    public PowerUpDecorator crear(IJugador envuelto) {
        return new PowerUpAntideslizante(envuelto);
    }

    @Override
    public PowerUpDecorator recrear(IJugador envuelto, int duracion, int usosRestantes) {
        return new PowerUpAntideslizante(envuelto, duracion);
    }
}
