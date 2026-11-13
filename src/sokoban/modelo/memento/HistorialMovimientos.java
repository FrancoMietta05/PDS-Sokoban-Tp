package sokoban.modelo.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Caretaker del patron Memento: apila y desapila fotos cuyo contenido no
 * puede leer (decision 4 de la bitacora).
 *
 * Aca viven las tres reglas de undo del enunciado, juntas y en un solo lugar:
 * se recuerdan los ultimos 15 movimientos, cada undo retrocede 5, y se permiten
 * hasta 3 usos consecutivos. "Saber restaurar" es de Juego; "decidir cuanto y
 * cuantas veces" es de esta clase.
 *
 * La racha de usos consecutivos se corta cuando el jugador vuelve a mover.
 */
public class HistorialMovimientos {

    public static final int MAX_MEMENTOS = 15;
    public static final int PASOS_POR_UNDO = 5;
    public static final int MAX_USOS_CONSECUTIVOS = 3;

    private final Deque<Memento> mementos = new ArrayDeque<>();
    private int usosConsecutivos;

    /** Apila una foto. Al pasar de 15 se descarta la mas vieja. */
    public void guardar(Memento m) {
        if (m == null) {
            return;
        }
        if (mementos.size() == MAX_MEMENTOS) {
            mementos.pollLast();
        }
        mementos.addFirst(m);
    }

    /**
     * Retrocede 5 movimientos.
     *
     * @return la foto a la que hay que volver, o null si no se puede deshacer
     *         (sin historial, o agotados los 3 usos consecutivos). Si hay menos
     *         de 5 fotos guardadas, vuelve a la mas antigua disponible.
     */
    public Memento deshacer() {
        if (!puedeDeshacer()) {
            return null;
        }
        Memento objetivo = null;
        for (int i = 0; i < PASOS_POR_UNDO && !mementos.isEmpty(); i++) {
            objetivo = mementos.pollFirst();
        }
        usosConsecutivos++;
        return objetivo;
    }

    public boolean puedeDeshacer() {
        return !mementos.isEmpty() && usosConsecutivos < MAX_USOS_CONSECUTIVOS;
    }

    /** La llama el controlador cuando el jugador hace un movimiento normal. */
    public void reiniciarUsosConsecutivos() {
        usosConsecutivos = 0;
    }

    /** Vacia el historial: cambio o reinicio de nivel. */
    public void limpiar() {
        mementos.clear();
        usosConsecutivos = 0;
    }

    public int getUsosConsecutivos() {
        return usosConsecutivos;
    }

    public int getCantidadGuardada() {
        return mementos.size();
    }
}
