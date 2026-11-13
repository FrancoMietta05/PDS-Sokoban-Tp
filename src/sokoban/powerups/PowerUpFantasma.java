package sokoban.powerups;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.celdas.Celda;
import sokoban.modelo.entidades.IJugador;

/**
 * Permite atravesar una cantidad limitada de paredes y muros cerrados.
 *
 * puedeAtravesar() es una consulta pura: no descuenta nada. El gasto ocurre
 * en notificarAtravesoPared(), que el tablero llama recien cuando el
 * movimiento ya se concreto. Si se descontara en la consulta, un intento
 * cancelado por otra regla igual gastaria una pared.
 */
public class PowerUpFantasma extends PowerUpDecorator {

    public static final int PAREDES_POR_DEFECTO = 3;

    private int paredesRestantes;

    public PowerUpFantasma(IJugador jugador) {
        this(jugador, PAREDES_POR_DEFECTO);
    }

    public PowerUpFantasma(IJugador jugador, int paredesRestantes) {
        super(jugador, ILIMITADO, paredesRestantes);
        this.paredesRestantes = paredesRestantes;
    }

    @Override
    public boolean puedeAtravesar(Celda c) {
        if (paredesRestantes > 0 && !c.esTransitable()) {
            return true;
        }
        return jugador.puedeAtravesar(c);
    }

    @Override
    public void notificarAtravesoPared() {
        if (paredesRestantes > 0) {
            paredesRestantes--;
            consumirUso();
        }
        jugador.notificarAtravesoPared();
    }

    @Override
    public boolean estaActivo() {
        return paredesRestantes > 0;
    }

    public int getParedesRestantes() {
        return paredesRestantes;
    }

    @Override
    public EstadoPowerUp capturarEstado() {
        return new EstadoPowerUp(getTipo(), getDuracion(), paredesRestantes);
    }

    @Override
    public TipoPowerUp getTipo() {
        return TipoPowerUp.FANTASMA;
    }
}
