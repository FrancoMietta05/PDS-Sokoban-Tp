package sokoban.powerups.fabricas;

import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.PowerUpDecorator;
import sokoban.powerups.PowerUpFantasma;

/** Creador concreto del Factory Method: fabrica PowerUpFantasma. */
public class FabricaPowerUpFantasma implements FabricaPowerUp {

    @Override
    public PowerUpDecorator crear(IJugador envuelto) {
        return new PowerUpFantasma(envuelto);
    }

    @Override
    public PowerUpDecorator recrear(IJugador envuelto, int duracion, int usosRestantes) {
        return new PowerUpFantasma(envuelto, usosRestantes);
    }
}
