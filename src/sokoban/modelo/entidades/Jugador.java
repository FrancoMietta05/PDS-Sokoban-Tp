package sokoban.modelo.entidades;

import sokoban.modelo.Direccion;
import sokoban.modelo.celdas.Celda;

/**
 * El almacenista. Lleva sus contadores y responde las reglas base: empuja una
 * caja a la vez, no atraviesa nada que no sea transitable, se desliza en el
 * hielo y no rompe cajas fragiles de un golpe.
 *
 * Los power-ups no se agregan aca: son decoradores que pisan una de esas
 * respuestas. Despues de introducir IJugador, a esta clase no le entra ninguna
 * dependencia: nadie la nombra salvo el cargador de niveles, todos hablan por
 * la interfaz.
 */
public class Jugador extends EntidadMovil implements IJugador {

    private int movimientos;
    private int empujes;

    public Jugador(int fila, int columna) {
        super(fila, columna);
    }

    @Override
    public void mover(Direccion d) {
        mover(getFila() + d.getDeltaFila(), getColumna() + d.getDeltaColumna());
        movimientos++;
    }

    @Override
    public int cajasEmpujables() {
        return 1;
    }

    @Override
    public boolean puedeAtravesar(Celda c) {
        return false;
    }

    @Override
    public boolean resisteDeslizamiento() {
        return false;
    }

    @Override
    public boolean rompeFragiles() {
        return false;
    }

    @Override
    public String descripcion() {
        return "Sokoban";
    }

    @Override
    public int getMovimientos() {
        return movimientos;
    }

    @Override
    public int getEmpujes() {
        return empujes;
    }

    @Override
    public void registrarEmpuje() {
        empujes++;
    }

    @Override
    public void posicionar(int fila, int columna) {
        mover(fila, columna);
    }

    @Override
    public void notificarAtravesoPared() {
        // El jugador base no atraviesa paredes: no tiene nada que descontar.
    }

    
    public void restaurarContadores(int movimientos, int empujes) {
        this.movimientos = movimientos;
        this.empujes = empujes;
    }
}
