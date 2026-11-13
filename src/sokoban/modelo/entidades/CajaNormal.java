package sokoban.modelo.entidades;

/** Caja comun: se empuja sin desgastarse y no tiene efecto al apoyarse. */
public class CajaNormal extends Caja {

    public CajaNormal(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public char getSimbolo() {
        return '$';
    }
}
