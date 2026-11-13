package sokoban.logros;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.TipoEvento;

/**
 * Se desbloquea al completar un nivel sin haber usado el boton de undo.
 *
 * Es el caso que justifica el gancho reiniciarEstadoNivel(): necesita recordar
 * algo que paso durante el nivel y olvidarlo cuando empieza el siguiente.
 */
public class LogroSinDeshacer extends Logro {

    private boolean usoUndo;

    public LogroSinDeshacer() {
        super("Pulso firme", "Completaste un nivel sin usar deshacer");
    }

    @Override
    public boolean verificar(EventoJuego e) {
        if (e.getTipo() == TipoEvento.UNDO_USADO) {
            usoUndo = true;
            return false;
        }
        return e.getTipo() == TipoEvento.NIVEL_COMPLETADO && !usoUndo;
    }

    @Override
    public void reiniciarEstadoNivel() {
        usoUndo = false;
    }
}
