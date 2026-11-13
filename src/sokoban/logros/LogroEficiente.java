package sokoban.logros;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.TipoEvento;

/** Se desbloquea al completar un nivel dentro de un tope de movimientos. */
public class LogroEficiente extends Logro {

    public static final int MAX_POR_DEFECTO = 40;

    private final int maxMovimientos;

    public LogroEficiente() {
        this(MAX_POR_DEFECTO);
    }

    public LogroEficiente(int maxMovimientos) {
        super("Eficiente", "Completaste un nivel en " + maxMovimientos + " movimientos o menos");
        this.maxMovimientos = maxMovimientos;
    }

    @Override
    public boolean verificar(EventoJuego e) {
        if (e.getTipo() != TipoEvento.NIVEL_COMPLETADO) {
            return false;
        }
        int movimientos = e.getDatoEntero(EventoJuego.CLAVE_MOVIMIENTOS, Integer.MAX_VALUE);
        return movimientos <= maxMovimientos;
    }

    public int getMaxMovimientos() {
        return maxMovimientos;
    }
}
