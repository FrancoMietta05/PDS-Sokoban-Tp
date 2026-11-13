package sokoban.modelo.celdas;

import java.util.ArrayList;
import java.util.List;

/**
 * Casillero cerrojo: al apoyarse una CajaLlave sobre el, abre los muros
 * asociados.
 *
 * Agrega los muros que gobierna (rombo hueco en el diagrama): los muros
 * existen como celdas del tablero por su cuenta, el cerrojo solo los conoce.
 * La lista permite varios muros por cerrojo; el cargador actual asocia todos
 * los muros del nivel a todos sus cerrojos, pero la estructura ya soporta
 * grupos si el formato del archivo los llega a declarar.
 */
public class CasillaCerrojo extends Celda {

    private final List<Muro> murosAsociados = new ArrayList<>();
    private boolean activado;

    public CasillaCerrojo(int fila, int columna) {
        super(fila, columna);
    }

    public void asociarMuro(Muro muro) {
        if (muro != null && !murosAsociados.contains(muro)) {
            murosAsociados.add(muro);
        }
    }

    /**
     * Abre los muros que gobierna.
     *
     * @return true si este cerrojo no estaba activado todavia, para que el
     *         tablero sepa si corresponde publicar el evento CERROJO_ABIERTO.
     */
    public boolean activar() {
        if (activado) {
            return false;
        }
        activado = true;
        for (Muro muro : murosAsociados) {
            muro.abrir();
        }
        return true;
    }

    public boolean estaActivado() {
        return activado;
    }

    /** Usado al restaurar un memento. */
    public void restaurarEstado(boolean activado) {
        this.activado = activado;
    }

    public List<Muro> getMurosAsociados() {
        return List.copyOf(murosAsociados);
    }

    @Override
    public boolean esTransitable() {
        return true;
    }
}
