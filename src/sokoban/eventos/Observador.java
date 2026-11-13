package sokoban.eventos;

/** Rol Observador (subscriber) del patron Observer. */
public interface Observador {

    void actualizar(EventoJuego e);
}
