package sokoban.modelo;

import java.util.ArrayList;
import java.util.List;

import sokoban.eventos.GestorEventos;
import sokoban.modelo.celdas.CasillaCerrojo;
import sokoban.modelo.celdas.CasillaDestino;
import sokoban.modelo.celdas.CasillaPowerUp;
import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.Muro;
import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.Jugador;

/**
 * Patron Builder: arma un Tablero paso a paso. Es el builder; el producto es
 * Tablero y el director es DirectorNivel, que sabe en que orden y con que
 * datos llamar a cada paso.
 *
 * Existe para sacarle a Tablero la responsabilidad de construirse: el tablero
 * solo conserva las reglas de movimiento. Ademas del armado, el builder se
 * encarga de lo que depende de tener todo el nivel a la vista: clasificar las
 * celdas especiales (muros, cerrojos, power-ups, destinos) en las listas que
 * despues usa el Memento, y asociar cada cerrojo con los muros que abre.
 *
 * El orden en que se agregan los elementos se conserva, y es el que el Memento
 * necesita para restaurar: las listas se capturan y se reponen por indice.
 */
public class TableroBuilder {

    private final GestorEventos gestorEventos;

    private int nivel;
    private Celda[][] celdas = new Celda[0][0];
    private final List<Caja> cajas = new ArrayList<>();
    private final List<Muro> muros = new ArrayList<>();
    private final List<CasillaCerrojo> cerrojos = new ArrayList<>();
    private final List<CasillaPowerUp> casillasPowerUp = new ArrayList<>();
    private final List<CasillaDestino> destinos = new ArrayList<>();
    private Jugador jugador;

    public TableroBuilder(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }

    /** Numero de nivel que el tablero usa al publicar eventos. */
    public TableroBuilder nivel(int nivel) {
        this.nivel = nivel;
        return this;
    }

    /** Paso 1: reserva la grilla. Descarta lo agregado hasta el momento. */
    public TableroBuilder dimensiones(int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("El tablero necesita al menos una fila y una columna");
        }
        celdas = new Celda[filas][columnas];
        cajas.clear();
        muros.clear();
        cerrojos.clear();
        casillasPowerUp.clear();
        destinos.clear();
        jugador = null;
        return this;
    }

    /** Paso 2: coloca la celda en la posicion que ella misma declara. */
    public TableroBuilder agregarCelda(Celda celda) {
        celdas[celda.getFila()][celda.getColumna()] = celda;
        if (celda instanceof Muro) {
            muros.add((Muro) celda);
        } else if (celda instanceof CasillaCerrojo) {
            cerrojos.add((CasillaCerrojo) celda);
        } else if (celda instanceof CasillaPowerUp) {
            casillasPowerUp.add((CasillaPowerUp) celda);
        } else if (celda instanceof CasillaDestino) {
            destinos.add((CasillaDestino) celda);
        }
        return this;
    }

    /** Paso 3 (opcional, repetible): suma una caja. */
    public TableroBuilder agregarCaja(Caja caja) {
        cajas.add(caja);
        return this;
    }

    /** Paso 4: define donde arranca el jugador. */
    public TableroBuilder colocarJugador(Jugador jugador) {
        this.jugador = jugador;
        return this;
    }

    /**
     * Paso final: valida que el nivel este completo, asocia cerrojos con muros
     * y entrega el Tablero.
     */
    public Tablero build() {
        if (jugador == null) {
            throw new IllegalArgumentException("El nivel no tiene jugador");
        }
        for (Celda[] fila : celdas) {
            for (Celda celda : fila) {
                if (celda == null) {
                    throw new IllegalStateException("El tablero tiene celdas sin definir");
                }
            }
        }
        asociarCerrojosConMuros();
        return new Tablero(gestorEventos, nivel, celdas, cajas, muros, cerrojos,
                casillasPowerUp, destinos, jugador);
    }

    /**
     * El formato actual declara un solo grupo de cerrojos por nivel: todos los
     * cerrojos abren todos los muros. La estructura (una lista de muros por
     * cerrojo) ya soporta grupos si el formato los llega a declarar.
     */
    private void asociarCerrojosConMuros() {
        for (CasillaCerrojo cerrojo : cerrojos) {
            for (Muro muro : muros) {
                cerrojo.asociarMuro(muro);
            }
        }
    }
}
