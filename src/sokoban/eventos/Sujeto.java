package sokoban.eventos;

/** Rol Sujeto (publisher) del patron Observer. */
public interface Sujeto {

    void agregarObservador(Observador o);

    void quitarObservador(Observador o);

    void notificar(EventoJuego e);
}
