package sokoban.modelo;

/**
 * Resultado de un intento de movimiento.
 *
 * ADICION RESPECTO DEL DIAGRAMA: en el diagrama Tablero.moverJugador(direccion)
 * devuelve void. El controlador necesita saber si el movimiento efectivamente
 * ocurrio para decidir si apila un memento y si descuenta duracion de los
 * power-ups; devolver un objeto de resultado evita que Juego tenga que
 * reconstruir esa informacion comparando estados.
 */
public final class ResultadoMovimiento {

    private static final ResultadoMovimiento SIN_MOVIMIENTO =
            new ResultadoMovimiento(false, false, null);

    private final boolean seMovio;
    private final boolean huboEmpuje;
    private final TipoPowerUp powerUpRecogido;

    public ResultadoMovimiento(boolean seMovio, boolean huboEmpuje, TipoPowerUp powerUpRecogido) {
        this.seMovio = seMovio;
        this.huboEmpuje = huboEmpuje;
        this.powerUpRecogido = powerUpRecogido;
    }

    public static ResultadoMovimiento sinMovimiento() {
        return SIN_MOVIMIENTO;
    }

    public boolean seMovio() {
        return seMovio;
    }

    public boolean huboEmpuje() {
        return huboEmpuje;
    }

    /** Puede ser null: solo trae valor si el jugador piso un power-up sin recoger. */
    public TipoPowerUp getPowerUpRecogido() {
        return powerUpRecogido;
    }
}
