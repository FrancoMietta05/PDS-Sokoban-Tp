package sokoban.powerups;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;
import sokoban.powerups.fabricas.FabricaPowerUp;
import sokoban.powerups.fabricas.FabricaPowerUpAntideslizante;
import sokoban.powerups.fabricas.FabricaPowerUpFantasma;
import sokoban.powerups.fabricas.FabricaPowerUpFuerza;
import sokoban.powerups.fabricas.FabricaPowerUpManosDeAcero;

/**
 * Unico lugar del sistema que sabe que fabrica corresponde a cada
 * TipoPowerUp (decision 6 de la bitacora). El resto del sistema maneja solo
 * el enum. Las clases concretas de decorador las conocen unicamente las
 * FabricaPowerUp (patron Factory Method): este gestor ya no tiene ningun
 * switch sobre el tipo.
 *
 * Tambien es el que arma y desarma la cadena de decoradores. Todos sus metodos
 * devuelven el IJugador resultante en lugar de modificar el recibido, porque
 * agregar o quitar una capa cambia el objeto de la punta: el llamador tiene
 * que reasignar su referencia.
 */
public class GestorPowerUps {

    private final Map<TipoPowerUp, FabricaPowerUp> fabricas = new EnumMap<>(TipoPowerUp.class);

    public GestorPowerUps() {
        fabricas.put(TipoPowerUp.FUERZA, new FabricaPowerUpFuerza());
        fabricas.put(TipoPowerUp.ANTIDESLIZANTE, new FabricaPowerUpAntideslizante());
        fabricas.put(TipoPowerUp.FANTASMA, new FabricaPowerUpFantasma());
        fabricas.put(TipoPowerUp.MANOS_DE_ACERO, new FabricaPowerUpManosDeAcero());
    }

    /** Envuelve al jugador con el decorador que corresponde al tipo recogido. */
    public IJugador aplicar(TipoPowerUp tipo, IJugador jugador) {
        if (tipo == null) {
            return jugador;
        }
        return fabricaDe(tipo).crear(jugador);
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
            resultado = fabricaDe(capa.getTipo())
                    .recrear(resultado, capa.getDuracion(), capa.getUsosRestantes());
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
            resultado = fabricaDe(estado.getTipo())
                    .recrear(resultado, estado.getDuracion(), estado.getUsosRestantes());
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

    /** Busca la fabrica del tipo; si el tipo no se registro es un error de programacion. */
    private FabricaPowerUp fabricaDe(TipoPowerUp tipo) {
        FabricaPowerUp fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new IllegalArgumentException("Power-up no contemplado: " + tipo);
        }
        return fabrica;
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
