package sokoban.powerups;

import java.util.ArrayList;
import java.util.List;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;

/**
 * Unico lugar del sistema que sabe que clase de decorador corresponde a cada
 * TipoPowerUp (decision 6 de la bitacora). El resto del sistema maneja solo
 * el enum.
 *
 * Tambien es el que arma y desarma la cadena de decoradores. Todos sus metodos
 * devuelven el IJugador resultante en lugar de modificar el recibido, porque
 * agregar o quitar una capa cambia el objeto de la punta: el llamador tiene
 * que reasignar su referencia.
 */
public class GestorPowerUps {

    /** Envuelve al jugador con el decorador que corresponde al tipo recogido. */
    public IJugador aplicar(TipoPowerUp tipo, IJugador jugador) {
        if (tipo == null) {
            return jugador;
        }
        return crear(tipo, jugador, -2, -2);
    }

    /**
     * Descuenta un movimiento a cada capa activa de la cadena.
     * No altera la estructura: solo los contadores.
     */
    public IJugador actualizarDuraciones(IJugador jugador) {
        IJugador actual = jugador;
        while (actual instanceof PowerUpDecorator) {
            PowerUpDecorator capa = (PowerUpDecorator) actual;
            capa.consumirTurno();
            actual = capa.getEnvuelto();
        }
        return jugador;
    }

    /**
     * Rearma la cadena dejando afuera las capas vencidas y devuelve la nueva
     * punta. El jugador base nunca se toca: por eso la relacion decorador -
     * envuelto es agregacion y no composicion.
     */
    public IJugador quitarExpirados(IJugador jugador) {
        List<PowerUpDecorator> vigentes = new ArrayList<>();
        IJugador base = jugador;
        while (base instanceof PowerUpDecorator) {
            PowerUpDecorator capa = (PowerUpDecorator) base;
            if (capa.estaActivo()) {
                vigentes.add(capa);
            }
            base = capa.getEnvuelto();
        }
        if (vigentes.size() == capas(jugador)) {
            return jugador;
        }
        // vigentes esta de la punta hacia adentro: se reconstruye al reves.
        IJugador resultado = base;
        for (int i = vigentes.size() - 1; i >= 0; i--) {
            PowerUpDecorator capa = vigentes.get(i);
            resultado = crear(capa.getTipo(), resultado, capa.getDuracion(), capa.getUsosRestantes());
        }
        return resultado;
    }

    /** Foto de la cadena, de la punta hacia adentro, para el memento. */
    public List<EstadoPowerUp> capturar(IJugador jugador) {
        List<EstadoPowerUp> estados = new ArrayList<>();
        IJugador actual = jugador;
        while (actual instanceof PowerUpDecorator) {
            PowerUpDecorator capa = (PowerUpDecorator) actual;
            estados.add(capa.capturarEstado());
            actual = capa.getEnvuelto();
        }
        return estados;
    }

    /**
     * Rearma la cadena guardada en un memento sobre el jugador base.
     * Resuelve el pendiente 1 de la bitacora: sin esto, un undo despues de
     * agarrar un power-up dejaba capas de mas o contadores que no correspondian.
     */
    public IJugador restaurar(List<EstadoPowerUp> estados, IJugador base) {
        IJugador resultado = desnudar(base);
        if (estados == null) {
            return resultado;
        }
        for (int i = estados.size() - 1; i >= 0; i--) {
            EstadoPowerUp estado = estados.get(i);
            resultado = crear(estado.getTipo(), resultado, estado.getDuracion(), estado.getUsosRestantes());
        }
        return resultado;
    }

    /** Devuelve el jugador que esta en el fondo de la cadena, sin decoradores. */
    public IJugador desnudar(IJugador jugador) {
        IJugador actual = jugador;
        while (actual instanceof PowerUpDecorator) {
            actual = ((PowerUpDecorator) actual).getEnvuelto();
        }
        return actual;
    }

    /**
     * Fabrica de decoradores: el unico switch sobre TipoPowerUp del sistema.
     * Con duracion o usos en -2 se usan los valores por defecto del power-up.
     */
    private PowerUpDecorator crear(TipoPowerUp tipo, IJugador envuelto, int duracion, int usos) {
        switch (tipo) {
            case FUERZA:
                return new PowerUpFuerza(envuelto,
                        duracion == -2 ? PowerUpFuerza.DURACION_POR_DEFECTO : duracion);
            case ANTIDESLIZANTE:
                return new PowerUpAntideslizante(envuelto,
                        duracion == -2 ? PowerUpAntideslizante.DURACION_POR_DEFECTO : duracion);
            case MANOS_DE_ACERO:
                return new PowerUpManosDeAcero(envuelto,
                        duracion == -2 ? PowerUpManosDeAcero.DURACION_POR_DEFECTO : duracion);
            case FANTASMA:
                return new PowerUpFantasma(envuelto,
                        usos == -2 ? PowerUpFantasma.PAREDES_POR_DEFECTO : usos);
            default:
                throw new IllegalArgumentException("Power-up no contemplado: " + tipo);
        }
    }

    private int capas(IJugador jugador) {
        int cantidad = 0;
        IJugador actual = jugador;
        while (actual instanceof PowerUpDecorator) {
            cantidad++;
            actual = ((PowerUpDecorator) actual).getEnvuelto();
        }
        return cantidad;
    }
}
