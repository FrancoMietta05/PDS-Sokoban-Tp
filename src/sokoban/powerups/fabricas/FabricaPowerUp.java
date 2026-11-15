package sokoban.powerups.fabricas;

import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.PowerUpDecorator;

/**
 * Patron Factory Method, lado de los power-ups. Creador abstracto: cada
 * fabrica concreta sabe que decorador corresponde a su TipoPowerUp.
 *
 * Reemplaza al switch que GestorPowerUps tenia sobre el enum: ahora el gestor
 * solo guarda un mapa TipoPowerUp -> FabricaPowerUp y no conoce las clases
 * concretas de decorador.
 */
public interface FabricaPowerUp {

    /** Metodo fabrica: envuelve al jugador con el power-up recien recogido. */
    PowerUpDecorator crear(IJugador envuelto);

    /**
     * Metodo fabrica para rearmar una capa que ya existia (undo o expiracion
     * de otra capa), conservando lo que le quedaba de duracion o de usos.
     */
    PowerUpDecorator recrear(IJugador envuelto, int duracion, int usosRestantes);
}
