package sokoban.logros;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.TipoEvento;

/** Se desbloquea al completar el primer nivel. */
public class LogroPrimerNivel extends Logro {

    public LogroPrimerNivel() {
        super("Primer paso", "Completaste tu primer nivel");
    }

    @Override
    public boolean verificar(EventoJuego e) {
        return e.getTipo() == TipoEvento.NIVEL_COMPLETADO && e.getNivel() == 1;
    }
}
