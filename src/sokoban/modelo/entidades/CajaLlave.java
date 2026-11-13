package sokoban.modelo.entidades;

import sokoban.modelo.celdas.CasillaCerrojo;
import sokoban.modelo.celdas.Celda;

/**
 * Caja que se mueve como una normal, pero al quedar apoyada sobre un
 * casillero cerrojo lo activa y con eso abre los muros asociados.
 *
 * El tablero no pregunta de que tipo es la caja: llama a alPosicionarseEn()
 * y es esta subclase la que decide que hacer.
 */
public class CajaLlave extends Caja {

    public CajaLlave(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public boolean alPosicionarseEn(Celda celda) {
        if (celda instanceof CasillaCerrojo) {
            return abrirCerrojo((CasillaCerrojo) celda);
        }
        return false;
    }

    public boolean abrirCerrojo(CasillaCerrojo cerrojo) {
        return cerrojo.activar();
    }

    @Override
    public char getSimbolo() {
        return 'L';
    }
}
