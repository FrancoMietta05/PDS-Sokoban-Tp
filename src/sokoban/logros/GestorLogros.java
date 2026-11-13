package sokoban.logros;

import java.util.ArrayList;
import java.util.List;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;

/**
 * Observador que escucha lo que pasa en la partida y va desbloqueando logros.
 *
 * Es la primera de las dos funcionalidades adicionales del trabajo, y la razon
 * por la que esta el Observer: agregar un logro nuevo es agregar una subclase
 * de Logro y sumarla a esta lista. No se toca ni Juego ni Tablero.
 *
 * Tambien publica: cuando desbloquea un logro emite LOGRO_DESBLOQUEADO por el
 * mismo GestorEventos, y asi el panel de notificaciones y el reproductor de
 * sonido se enteran sin que esta clase los conozca.
 */
public class GestorLogros implements Observador {

    private final List<Logro> logros = new ArrayList<>();
    private final GestorEventos gestorEventos;

    public GestorLogros(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
        logros.add(new LogroPrimerNivel());
        logros.add(new LogroSinDeshacer());
        logros.add(new LogroEficiente());
        logros.add(new LogroDemoledor());
        logros.add(new LogroCerrajero());
    }

    @Override
    public void actualizar(EventoJuego e) {
        if (e.getTipo() == TipoEvento.LOGRO_DESBLOQUEADO) {
            return; // No se reacciona a los propios avisos.
        }
        if (e.getTipo() == TipoEvento.NIVEL_INICIADO) {
            for (Logro logro : logros) {
                logro.reiniciarEstadoNivel();
            }
            return;
        }
        for (Logro logro : logros) {
            if (logro.estaDesbloqueado()) {
                continue;
            }
            if (logro.verificar(e)) {
                logro.desbloquear();
                gestorEventos.notificar(EventoJuego.con(TipoEvento.LOGRO_DESBLOQUEADO,
                        e.getNivel(), EventoJuego.CLAVE_LOGRO, logro));
            }
        }
    }

    public List<Logro> getLogros() {
        return List.copyOf(logros);
    }

    public List<Logro> logrosDesbloqueados() {
        List<Logro> desbloqueados = new ArrayList<>();
        for (Logro logro : logros) {
            if (logro.estaDesbloqueado()) {
                desbloqueados.add(logro);
            }
        }
        return desbloqueados;
    }

    /** Partida nueva: se borra todo el progreso de logros. */
    public void reiniciar() {
        for (Logro logro : logros) {
            logro.reiniciar();
        }
    }
}
