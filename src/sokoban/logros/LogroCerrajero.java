package sokoban.logros;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.TipoEvento;

/** Se desbloquea al abrir un cerrojo con una caja llave. */
public class LogroCerrajero extends Logro {

    public LogroCerrajero() {
        super("Cerrajero", "Abriste un cerrojo con una caja llave");
    }

    @Override
    public boolean verificar(EventoJuego e) {
        return e.getTipo() == TipoEvento.CERROJO_ABIERTO;
    }
}
