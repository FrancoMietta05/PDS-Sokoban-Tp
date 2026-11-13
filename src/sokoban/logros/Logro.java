package sokoban.logros;

import sokoban.eventos.EventoJuego;

/**
 * Logro desbloqueable. Cada subclase encapsula una condicion distinta y el
 * GestorLogros no sabe cual es: solo les pasa el evento y pregunta.
 *
 * reiniciarEstadoNivel() es el gancho para los logros que llevan estado
 * acumulado dentro de un nivel (por ejemplo, terminar sin usar undo): se llama
 * al arrancar cada nivel y por defecto no hace nada. El flag de desbloqueado
 * no se toca ahi, porque un logro ya conseguido no se pierde.
 */
public abstract class Logro {

    private final String nombre;
    private final String descripcion;
    private boolean desbloqueado;

    protected Logro(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Devuelve true si este evento completa la condicion del logro. */
    public abstract boolean verificar(EventoJuego e);

    public void desbloquear() {
        this.desbloqueado = true;
    }

    public boolean estaDesbloqueado() {
        return desbloqueado;
    }

    public void reiniciarEstadoNivel() {
        // Por defecto el logro no acumula estado por nivel.
    }

    /** Reinicio total: partida nueva. */
    public void reiniciar() {
        desbloqueado = false;
        reiniciarEstadoNivel();
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
