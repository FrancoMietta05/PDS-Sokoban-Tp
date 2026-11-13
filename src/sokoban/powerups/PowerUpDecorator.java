package sokoban.powerups;

import sokoban.modelo.Direccion;
import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.celdas.Celda;
import sokoban.modelo.entidades.IJugador;

/**
 * Decorador abstracto de IJugador: agrega una habilidad al jugador sin tocar
 * la clase Jugador ni las que la usan.
 *
 * Guarda una referencia al IJugador que envuelve (agregacion, no composicion
 * -decision 3 de la bitacora-: el jugador existe desde antes del power-up y
 * sigue existiendo cuando este se vence; quitarExpirados desarma la capa y
 * devuelve intacto lo que habia adentro).
 *
 * Toda la delegacion esta aca. Cada decorador concreto pisa exactamente el
 * metodo de la regla que modifica y hereda el resto.
 *
 * Dos contadores independientes gobiernan la vigencia:
 *   - duracion: movimientos que le quedan al power-up (-1 = ilimitada).
 *   - usosRestantes: veces que puede ejercerse (-1 = ilimitadas).
 */
public abstract class PowerUpDecorator implements IJugador {

    public static final int ILIMITADO = -1;

    protected final IJugador jugador;
    private int duracion;
    private int usosRestantes;

    protected PowerUpDecorator(IJugador jugador, int duracion, int usosRestantes) {
        this.jugador = jugador;
        this.duracion = duracion;
        this.usosRestantes = usosRestantes;
    }

    /** Que power-up es esta capa. Lo usan el memento y el HUD. */
    public abstract TipoPowerUp getTipo();

    public boolean estaActivo() {
        return duracion != 0 && usosRestantes != 0;
    }

    public void consumirUso() {
        if (usosRestantes > 0) {
            usosRestantes--;
        }
    }

    /**
     * Descuenta un movimiento de la duracion.
     *
     * ADICION RESPECTO DEL DIAGRAMA: el diagrama tiene consumirUso() pero no
     * la contraparte para la duracion, que GestorPowerUps.actualizarDuraciones()
     * necesita. Son dos ejes distintos de vencimiento, por eso son dos metodos.
     */
    public void consumirTurno() {
        if (duracion > 0) {
            duracion--;
        }
    }

    public IJugador getEnvuelto() {
        return jugador;
    }

    public int getDuracion() {
        return duracion;
    }

    public int getUsosRestantes() {
        return usosRestantes;
    }

    /** Foto de esta capa, para guardarla en un memento. */
    public EstadoPowerUp capturarEstado() {
        return new EstadoPowerUp(getTipo(), duracion, usosRestantes);
    }

    // ------------------------------------------------------------------
    // Delegacion: por defecto toda pregunta viaja hacia adentro de la cadena.
    // ------------------------------------------------------------------

    @Override
    public void mover(Direccion d) {
        jugador.mover(d);
    }

    @Override
    public int getFila() {
        return jugador.getFila();
    }

    @Override
    public int getColumna() {
        return jugador.getColumna();
    }

    @Override
    public int cajasEmpujables() {
        return jugador.cajasEmpujables();
    }

    @Override
    public boolean puedeAtravesar(Celda c) {
        return jugador.puedeAtravesar(c);
    }

    @Override
    public boolean resisteDeslizamiento() {
        return jugador.resisteDeslizamiento();
    }

    @Override
    public boolean rompeFragiles() {
        return jugador.rompeFragiles();
    }

    @Override
    public String descripcion() {
        return getTipo().getNombre() + " + " + jugador.descripcion();
    }

    @Override
    public int getMovimientos() {
        return jugador.getMovimientos();
    }

    @Override
    public int getEmpujes() {
        return jugador.getEmpujes();
    }

    @Override
    public void registrarEmpuje() {
        jugador.registrarEmpuje();
    }

    @Override
    public void posicionar(int fila, int columna) {
        jugador.posicionar(fila, columna);
    }

    @Override
    public void notificarAtravesoPared() {
        jugador.notificarAtravesoPared();
    }

    @Override
    public void restaurarContadores(int movimientos, int empujes) {
        jugador.restaurarContadores(movimientos, empujes);
    }
}
