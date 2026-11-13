package sokoban.eventos;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion concreta del Sujeto.
 *
 * Es una clase propia y no un rol de Juego (decision 1 de la bitacora) por
 * dos razones: Juego ya tiene la responsabilidad de llevar la partida, y
 * varios eventos nacen en Tablero, que de otro modo tendria que conocer al
 * controlador. Dependiendo de este servicio -chico y sin logica de juego-
 * Tablero apunta a algo estable.
 */
public class GestorEventos implements Sujeto {

    private final List<Observador> observadores = new ArrayList<>();

    @Override
    public void agregarObservador(Observador o) {
        if (o != null && !observadores.contains(o)) {
            observadores.add(o);
        }
    }

    @Override
    public void quitarObservador(Observador o) {
        observadores.remove(o);
    }

    /**
     * Se recorre una copia de la lista: un observador puede, al reaccionar,
     * publicar otro evento (GestorLogros emite LOGRO_DESBLOQUEADO) o darse
     * de baja, y eso no debe romper la iteracion en curso.
     */
    @Override
    public void notificar(EventoJuego e) {
        for (Observador o : new ArrayList<>(observadores)) {
            o.actualizar(e);
        }
    }
}
