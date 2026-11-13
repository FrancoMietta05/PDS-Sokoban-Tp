package sokoban.modelo.entidades;

import sokoban.modelo.celdas.Celda;

/**
 * Caja del almacen. Define las reglas comunes de empuje y deja dos ganchos
 * polimorficos para que las subclases agreguen su comportamiento propio sin
 * que el tablero tenga que preguntar de que tipo es cada caja.
 *
 * Una caja rota no se saca de la lista del tablero: se marca como rota y deja
 * de contar para colisiones y para la victoria. Eso es lo que permite que el
 * undo la devuelva intacta.
 */
public abstract class Caja extends EntidadMovil {

    private boolean rota;

    protected Caja(int fila, int columna) {
        super(fila, columna);
    }

    /**
     * Gancho: avisa a la caja que acaba de ser empujada.
     *
     * @param rompeFragiles si el jugador tiene la habilidad de romper cajas
     *                      fragiles de un solo empuje.
     */
    public void registrarEmpuje(boolean rompeFragiles) {
        // Por defecto el empuje no desgasta la caja.
    }

    /**
     * Gancho: avisa a la caja en que celda quedo apoyada.
     *
     * @return true si de eso resulto un cambio en el tablero que merece un
     *         evento (hoy: una caja llave que activo un cerrojo).
     */
    public boolean alPosicionarseEn(Celda celda) {
        return false;
    }

    public boolean estaRota() {
        return rota;
    }

    protected void marcarRota() {
        this.rota = true;
    }

    /** Resistencia restante, o -1 si la caja no se desgasta. */
    public int getResistencia() {
        return -1;
    }

    /** Usado al restaurar un memento. */
    public void restaurarEstado(int fila, int columna, int resistencia, boolean rota) {
        mover(fila, columna);
        this.rota = rota;
    }

    /** Identifica el tipo para la vista y para los archivos de nivel. */
    public abstract char getSimbolo();
}
