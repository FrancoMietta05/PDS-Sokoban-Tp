package sokoban.eventos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mensaje inmutable que el Sujeto hace circular entre los observadores.
 *
 * Lleva el tipo, el nivel en el que ocurrio y un mapa de datos sueltos. El
 * mapa evita tener una subclase de evento por cada cosa que haya que informar:
 * agregar un dato nuevo no obliga a tocar la jerarquia ni a los observadores
 * que no lo miran.
 */
public final class EventoJuego {

    public static final String CLAVE_MOVIMIENTOS = "movimientos";
    public static final String CLAVE_EMPUJES = "empujes";
    public static final String CLAVE_UNDOS = "undos";
    public static final String CLAVE_PUNTAJE = "puntaje";
    public static final String CLAVE_POWERUP = "powerup";
    public static final String CLAVE_LOGRO = "logro";

    private final TipoEvento tipo;
    private final int nivel;
    private final Map<String, Object> datos;

    public EventoJuego(TipoEvento tipo, int nivel) {
        this(tipo, nivel, new HashMap<>());
    }

    public EventoJuego(TipoEvento tipo, int nivel, Map<String, Object> datos) {
        this.tipo = tipo;
        this.nivel = nivel;
        this.datos = Collections.unmodifiableMap(new HashMap<>(datos));
    }

    /** Constructor de conveniencia: pares clave/valor alternados. */
    public static EventoJuego con(TipoEvento tipo, int nivel, Object... paresClaveValor) {
        if (paresClaveValor.length % 2 != 0) {
            throw new IllegalArgumentException("Se esperaban pares clave/valor");
        }
        Map<String, Object> mapa = new HashMap<>();
        for (int i = 0; i < paresClaveValor.length; i += 2) {
            mapa.put(String.valueOf(paresClaveValor[i]), paresClaveValor[i + 1]);
        }
        return new EventoJuego(tipo, nivel, mapa);
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public int getNivel() {
        return nivel;
    }

    public Object getDato(String clave) {
        return datos.get(clave);
    }

    public int getDatoEntero(String clave, int valorPorDefecto) {
        Object valor = datos.get(clave);
        return (valor instanceof Integer) ? (Integer) valor : valorPorDefecto;
    }
}
