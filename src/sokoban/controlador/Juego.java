package sokoban.controlador;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.TipoEvento;
import sokoban.modelo.Direccion;
import sokoban.modelo.DirectorNivel;
import sokoban.modelo.ResultadoMovimiento;
import sokoban.modelo.Tablero;
import sokoban.modelo.TableroBuilder;
import sokoban.modelo.TipoPowerUp;
import sokoban.modelo.entidades.IJugador;
import sokoban.modelo.memento.HistorialMovimientos;
import sokoban.modelo.memento.Memento;
import sokoban.powerups.GestorPowerUps;

/**
 * Controlador de la partida: carga niveles (pidiendoselos al DirectorNivel y
 * al TableroBuilder, patron Builder), recibe la entrada ya traducida,
 * coordina el undo y decide cuando termino el nivel.
 *
 * En el Memento cumple el rol de originator: es el unico que sabe armar la
 * foto del estado (crearMemento) y volver a una (restaurar). Quien decide
 * cuando y cuanto se retrocede es HistorialMovimientos, el caretaker, que
 * guarda esas fotos sin poder abrirlas.
 *
 * Su campo de jugador esta declarado IJugador, no Jugador: ahi adentro puede
 * haber el jugador crudo o una cadena de decoradores de power-up, y al
 * controlador le da igual.
 *
 * No conoce Swing. Recibe Direccion y no KeyEvent, y avisa lo que pasa
 * publicando eventos; es la vista la que se suscribe y traduce las teclas.
 *
 * Patron Singleton: hay una unica partida por aplicacion, asi que Juego tiene
 * constructor privado y se obtiene con getInstance(). Como la instancia es
 * unica, tambien es duena de sus colaboradores (GestorEventos y
 * GestorPowerUps): los crea ella y los expone con getGestorEventos(), que es
 * por donde la vista y los logros se suscriben. reiniciarInstancia() existe
 * solo para que las pruebas puedan arrancar de cero.
 */
public class Juego {

    public static final String CARPETA_NIVELES = "niveles";
    public static final int PRIMER_NIVEL = 1;

    private static Juego instancia;

    private final GestorEventos gestorEventos;
    private final GestorPowerUps gestorPowerUps;
    private final HistorialMovimientos historial = new HistorialMovimientos();
    private final DirectorNivel directorNivel = new DirectorNivel();

    private Tablero tableroActual;
    private IJugador jugador;
    private int nivelActual;
    private boolean nivelCompletado;
    private int usosUndo;

    private Juego() {
        this.gestorEventos = new GestorEventos();
        this.gestorPowerUps = new GestorPowerUps();
    }

    /** Unica instancia de la partida; se crea la primera vez que se pide. */
    public static synchronized Juego getInstance() {
        if (instancia == null) {
            instancia = new Juego();
        }
        return instancia;
    }

    /** Descarta la instancia actual. Solo para pruebas: la proxima llamada crea una nueva. */
    public static synchronized void reiniciarInstancia() {
        instancia = null;
    }

    // ==================================================================
    // Ciclo de vida de la partida
    // ==================================================================

    public void iniciarJuego() {
        nivelActual = PRIMER_NIVEL;
        cargarNivel(nivelActual);
    }

    public void cargarNivel(int numero) {
        TableroBuilder builder = new TableroBuilder(gestorEventos).nivel(numero);
        Tablero tablero = directorNivel.construir(rutaNivel(numero), builder);

        this.tableroActual = tablero;
        this.nivelActual = numero;
        this.jugador = tablero.getJugador();
        this.nivelCompletado = false;
        this.usosUndo = 0;
        historial.limpiar();

        gestorEventos.notificar(new EventoJuego(TipoEvento.NIVEL_INICIADO, numero));
    }

    public void reiniciarNivel() {
        cargarNivel(nivelActual);
    }

    /**
     * Avanza al nivel siguiente si existe.
     *
     * @return false si no hay mas niveles, es decir, si el juego termino.
     */
    public boolean siguienteNivel() {
        if (!existeNivel(nivelActual + 1)) {
            return false;
        }
        cargarNivel(nivelActual + 1);
        return true;
    }

    public boolean existeNivel(int numero) {
        return Files.exists(Path.of(rutaNivel(numero)))
                || Juego.class.getResource("/" + rutaNivel(numero)) != null;
    }

    private String rutaNivel(int numero) {
        return CARPETA_NIVELES + "/nivel" + numero + ".txt";
    }

    // ==================================================================
    // Entrada del jugador
    // ==================================================================

    /**
     * Resuelve un movimiento completo: saca la foto previa, delega las reglas
     * al tablero y, si el movimiento ocurrio, apila la foto, envejece los
     * power-ups, aplica el que se haya recogido y verifica la victoria.
     *
     * La foto se saca antes de mover y se apila despues: si el movimiento no
     * era legal, el historial no se ensucia con estados repetidos y un undo
     * siempre retrocede 5 movimientos reales.
     */
    public void manejarEntrada(Direccion direccion) {
        if (direccion == null || tableroActual == null || nivelCompletado) {
            return;
        }

        Memento antes = crearMemento();
        ResultadoMovimiento resultado = tableroActual.moverJugador(direccion);
        if (!resultado.seMovio()) {
            return;
        }

        historial.guardar(antes);
        historial.reiniciarUsosConsecutivos();

        envejecerPowerUps();
        if (resultado.getPowerUpRecogido() != null) {
            aplicarPowerUp(resultado.getPowerUpRecogido());
        }

        if (verificarVictoria()) {
            completarNivel();
        }
    }

    /**
     * Retrocede 5 movimientos, si el caretaker lo permite.
     *
     * @return true si efectivamente se deshizo algo.
     */
    public boolean deshacerMovimiento() {
        if (nivelCompletado) {
            return false;
        }
        Memento memento = historial.deshacer();
        if (memento == null) {
            return false;
        }
        restaurar(memento);
        usosUndo++;
        gestorEventos.notificar(new EventoJuego(TipoEvento.UNDO_USADO, nivelActual));
        return true;
    }

    public boolean verificarVictoria() {
        return tableroActual != null && tableroActual.todasLasCajasEnDestino();
    }

    private void completarNivel() {
        nivelCompletado = true;
        gestorEventos.notificar(EventoJuego.con(TipoEvento.NIVEL_COMPLETADO, nivelActual,
                EventoJuego.CLAVE_MOVIMIENTOS, getMovimientos(),
                EventoJuego.CLAVE_EMPUJES, getEmpujes(),
                EventoJuego.CLAVE_UNDOS, usosUndo,
                EventoJuego.CLAVE_PUNTAJE, calcularPuntaje()));
    }

    // ==================================================================
    // Memento: rol originator
    // ==================================================================

    /**
     * Arma la foto del estado actual. Nadie mas sabe hacerlo, y nadie mas sabe
     * leerla: por eso el estado de la partida no necesita getters publicos de
     * posiciones ni de contadores para que el undo funcione.
     */
    public Memento crearMemento() {
        return new Memento(
                new Point(jugador.getColumna(), jugador.getFila()),
                jugador.getMovimientos(),
                jugador.getEmpujes(),
                tableroActual.capturarCajas(),
                tableroActual.capturarMuros(),
                tableroActual.capturarCerrojos(),
                tableroActual.capturarPowerUps(),
                gestorPowerUps.capturar(jugador));
    }

    /**
     * Repone un estado guardado: tablero, contadores, posicion y la cadena de
     * power-ups que estaba activa en ese momento.
     */
    public void restaurar(Memento memento) {
        tableroActual.restaurar(memento);

        // La cadena se rearma desde cero sobre el jugador base, asi no quedan
        // capas de mas ni usos restantes que no correspondan.
        jugador = gestorPowerUps.restaurar(memento.getPowerUpsActivos(), jugador);
        tableroActual.setJugador(jugador);

        Point pos = memento.getPosJugador();
        jugador.posicionar(pos.y, pos.x);
        jugador.restaurarContadores(memento.getMovimientos(), memento.getEmpujes());
    }

    // ==================================================================
    // Power-ups
    // ==================================================================

    /** Envuelve al jugador con el power-up recogido. */
    public void aplicarPowerUp(TipoPowerUp tipo) {
        jugador = gestorPowerUps.aplicar(tipo, jugador);
        tableroActual.setJugador(jugador);
    }

    /** Descuenta duracion y desarma las capas vencidas. */
    private void envejecerPowerUps() {
        jugador = gestorPowerUps.actualizarDuraciones(jugador);
        jugador = gestorPowerUps.quitarExpirados(jugador);
        tableroActual.setJugador(jugador);
    }

    // ==================================================================
    // Puntaje
    // ==================================================================

    /**
     * Criterio definido por el equipo: se arranca de una base y se descuenta
     * por lo que encarece la solucion. Los empujes pesan mas que los
     * movimientos porque son la jugada que hay que planificar, y el undo pesa
     * mas todavia porque es la ayuda. Nunca baja de cero.
     */
    public int calcularPuntaje() {
        int puntaje = 1000
                - getMovimientos() * 5
                - getEmpujes() * 10
                - usosUndo * 100;
        return Math.max(0, puntaje);
    }

    // ==================================================================
    // Consultas para la vista
    // ==================================================================

    public GestorEventos getGestorEventos() {
        return gestorEventos;
    }

    public Tablero getTableroActual() {
        return tableroActual;
    }

    public IJugador getJugador() {
        return jugador;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public int getMovimientos() {
        return jugador == null ? 0 : jugador.getMovimientos();
    }

    public int getEmpujes() {
        return jugador == null ? 0 : jugador.getEmpujes();
    }

    public int getUsosUndo() {
        return usosUndo;
    }

    public boolean estaCompletado() {
        return nivelCompletado;
    }

    public boolean puedeDeshacer() {
        return !nivelCompletado && historial.puedeDeshacer();
    }

    public HistorialMovimientos getHistorial() {
        return historial;
    }
}
