package sokoban.powerups;

import sokoban.modelo.TipoPowerUp;

/**
 * Foto de una capa de power-up: que tipo es y cuanto le queda.
 *
 * ADICION RESPECTO DEL DIAGRAMA (pendiente 1 de la bitacora): el Memento
 * original no guardaba los power-ups activos, asi que un undo dejaba la cadena
 * de decoradores desincronizada con el resto del estado. Con esto el memento
 * guarda la cadena entera y GestorPowerUps la sabe rearmar.
 *
 * Vive en este paquete para que la dependencia vaya memento hacia powerups y
 * no al reves: el subsistema de power-ups no tiene que conocer al Memento.
 */
public final class EstadoPowerUp {

    private final TipoPowerUp tipo;
    private final int duracion;
    private final int usosRestantes;

    public EstadoPowerUp(TipoPowerUp tipo, int duracion, int usosRestantes) {
        this.tipo = tipo;
        this.duracion = duracion;
        this.usosRestantes = usosRestantes;
    }

    public TipoPowerUp getTipo() {
        return tipo;
    }

    public int getDuracion() {
        return duracion;
    }

    public int getUsosRestantes() {
        return usosRestantes;
    }
}
