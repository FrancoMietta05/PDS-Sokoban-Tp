import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sokoban.controlador.Juego;
import sokoban.modelo.Direccion;
import sokoban.modelo.memento.Memento;
import sokoban.powerups.EstadoPowerUp;

/**
 * Busqueda en anchura sobre el juego real: prueba todos los movimientos
 * posibles a partir del estado inicial de un nivel y devuelve la solucion mas
 * corta, o null si no existe.
 *
 * No reimplementa ninguna regla: cada paso lo resuelve Juego.manejarEntrada(),
 * y para volver atras usa el propio Memento del juego. Asi lo que el
 * solucionador dice que se puede (o no se puede) hacer es exactamente lo que
 * el juego permite.
 *
 * Con sinPowerUps = true, despues de cada paso le saca al jugador la cadena de
 * decoradores restaurando un memento con la lista de power-ups vacia. Sirve
 * para demostrar que un nivel no se puede resolver sin usar el power-up.
 */
public final class Solucionador {

    private static final Direccion[] DIRECCIONES = {
            Direccion.ARRIBA, Direccion.ABAJO, Direccion.IZQUIERDA, Direccion.DERECHA};
    private static final char[] LETRAS = {'U', 'D', 'L', 'R'};

    /** Tope de estados explorados, para que un nivel mal disenado no cuelgue la bateria. */
    public static final int MAX_ESTADOS = 2_000_000;

    private Solucionador() {
    }

    /**
     * @return la secuencia mas corta (letras U/D/L/R) que resuelve el nivel,
     *         o null si no hay ninguna.
     * @throws IllegalStateException si se supero MAX_ESTADOS sin una respuesta.
     */
    public static String resolver(Juego juego, int nivel, boolean sinPowerUps) {
        juego.cargarNivel(nivel);
        Memento inicial = juego.crearMemento();

        Map<String, String> caminos = new HashMap<>();
        ArrayDeque<Memento> pendientes = new ArrayDeque<>();
        caminos.put(clave(inicial), "");
        pendientes.add(inicial);

        while (!pendientes.isEmpty()) {
            Memento actual = pendientes.poll();
            String camino = caminos.get(clave(actual));

            for (int i = 0; i < DIRECCIONES.length; i++) {
                // El memento repone todo el tablero; el nivel nunca queda
                // completado porque al ganar se devuelve antes.
                juego.restaurar(actual);
                int movimientosAntes = juego.getMovimientos();
                juego.manejarEntrada(DIRECCIONES[i]);
                if (juego.getMovimientos() == movimientosAntes) {
                    continue; // movimiento ilegal
                }
                if (juego.estaCompletado()) {
                    return camino + LETRAS[i];
                }
                Memento siguiente = juego.crearMemento();
                if (sinPowerUps) {
                    siguiente = sinCadena(siguiente);
                    juego.restaurar(siguiente);
                }
                String k = clave(siguiente);
                if (!caminos.containsKey(k)) {
                    if (caminos.size() >= MAX_ESTADOS) {
                        throw new IllegalStateException("El nivel " + nivel
                                + " supera " + MAX_ESTADOS + " estados");
                    }
                    caminos.put(k, camino + LETRAS[i]);
                    pendientes.add(siguiente);
                }
            }
        }
        return null;
    }

    private static Memento sinCadena(Memento m) {
        return new Memento(m.getFilaJugador(), m.getColumnaJugador(), m.getMovimientos(), m.getEmpujes(),
                m.getEstadoCajas(), m.getMurosAbiertos(), m.getCerrojosActivados(),
                m.getPowerUpsRecogidos(), List.of());
    }

    /** Todo lo que distingue un estado de otro, menos los contadores. */
    private static String clave(Memento m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getFilaJugador()).append(',').append(m.getColumnaJugador()).append('|');
        for (Memento.EstadoCaja c : m.getEstadoCajas()) {
            sb.append(c.getFila()).append(',').append(c.getColumna()).append(',')
                    .append(c.getResistencia()).append(c.estaRota() ? 'x' : 'o').append(';');
        }
        sb.append('|').append(m.getMurosAbiertos())
                .append('|').append(m.getCerrojosActivados())
                .append('|').append(m.getPowerUpsRecogidos()).append('|');
        for (EstadoPowerUp p : m.getPowerUpsActivos()) {
            sb.append(p.getTipo()).append(',').append(p.getDuracion()).append(',')
                    .append(p.getUsosRestantes()).append(';');
        }
        return sb.toString();
    }
}
