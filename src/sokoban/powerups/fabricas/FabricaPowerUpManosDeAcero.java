package sokoban.powerups.fabricas;

import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.PowerUpDecorator;
import sokoban.powerups.PowerUpManosDeAcero;

/** Creador concreto del Factory Method: fabrica PowerUpManosDeAcero. */
public class FabricaPowerUpManosDeAcero implements FabricaPowerUp {

    @Override
    public PowerUpDecorator crear(IJugador envuelto) {
        return new PowerUpManosDeAcero(envuelto);
    }

    @Override
    public PowerUpDecorator recrear(IJugador envuelto, int duracion, int usosRestantes) {
        return new PowerUpManosDeAcero(envuelto, duracion);
    }
}
