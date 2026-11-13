package sokoban.logros;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.TipoEvento;

/** Se desbloquea al romper una cantidad de cajas fragiles durante el juego. */
public class LogroDemoledor extends Logro {

    public static final int OBJETIVO_POR_DEFECTO = 3;

    private final int objetivo;
    private int rotas;

    public LogroDemoledor() {
        this(OBJETIVO_POR_DEFECTO);
    }

    public LogroDemoledor(int objetivo) {
        super("Demoledor", "Rompiste " + objetivo + " cajas fragiles");
        this.objetivo = objetivo;
    }

    @Override
    public boolean verificar(EventoJuego e) {
        if (e.getTipo() != TipoEvento.CAJA_ROTA) {
            return false;
        }
        rotas++;
        return rotas >= objetivo;
    }

    public int getRotas() {
        return rotas;
    }

    @Override
    public void reiniciar() {
        super.reiniciar();
        rotas = 0;
    }
}
