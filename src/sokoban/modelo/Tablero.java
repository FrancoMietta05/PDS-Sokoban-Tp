package sokoban.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.TipoEvento;
import sokoban.modelo.celdas.CasillaCerrojo;
import sokoban.modelo.celdas.CasillaDestino;
import sokoban.modelo.celdas.CasillaPowerUp;
import sokoban.modelo.celdas.Celda;
import sokoban.modelo.celdas.EspacioVacio;
import sokoban.modelo.celdas.Muro;
import sokoban.modelo.celdas.Pared;
import sokoban.modelo.celdas.TerrenoResbaladizo;
import sokoban.modelo.entidades.Caja;
import sokoban.modelo.entidades.CajaFragil;
import sokoban.modelo.entidades.CajaLlave;
import sokoban.modelo.entidades.CajaNormal;
import sokoban.modelo.entidades.IJugador;
import sokoban.modelo.entidades.Jugador;
import sokoban.modelo.memento.Memento;

/**
 * El almacen: la grilla de celdas, las cajas y el jugador que esta parado
 * sobre ellas. Resuelve las reglas de movimiento, porque es el unico objeto
 * que conoce a la vez las celdas vecinas y las cajas.
 *
 * Compone sus celdas y sus cajas (rombo relleno en el diagrama: se crean y se
 * mueren con el tablero) y solo conoce al jugador (rombo hueco: lo crea al
 * cargar el nivel y despues lo administra Juego, que puede reemplazarlo por
 * una cadena decorada).
 *
 * Publica eventos a traves de GestorEventos y no a traves de Juego: varios
 * eventos nacen aca -el empuje, la caja rota, el cerrojo abierto- y hacerlo
 * contra el controlador seria una dependencia hacia arriba (decision 1 de la
 * bitacora).
 *
 * Antes de resolver un movimiento le pregunta al jugador por la interfaz
 * IJugador; nunca por la clase Jugador. Asi no distingue entre el jugador
 * crudo y uno con power-ups encima.
 */
public class Tablero {

    /** Mapa de caracteres del archivo .txt a elementos del tablero. */
    public static final char CH_PARED = '#';
    public static final char CH_VACIO = ' ';
    public static final char CH_DESTINO = '.';
    public static final char CH_JUGADOR = '@';
    public static final char CH_JUGADOR_EN_DESTINO = '+';
    public static final char CH_CAJA = '$';
    public static final char CH_CAJA_EN_DESTINO = '*';
    public static final char CH_CAJA_FRAGIL = 'F';
    public static final char CH_CAJA_LLAVE = 'L';
    public static final char CH_RESBALADIZO = '~';
    public static final char CH_CERROJO = 'c';
    public static final char CH_MURO_CERRADO = 'M';
    public static final char CH_MURO_ABIERTO = 'm';

    private final GestorEventos gestorEventos;

    private Celda[][] celdas = new Celda[0][0];
    private final List<Caja> cajas = new ArrayList<>();
    private final List<Muro> muros = new ArrayList<>();
    private final List<CasillaCerrojo> cerrojos = new ArrayList<>();
    private final List<CasillaPowerUp> casillasPowerUp = new ArrayList<>();
    private final List<CasillaDestino> destinos = new ArrayList<>();

    private IJugador jugador;
    private int filas;
    private int columnas;
    private int nivel;

    public Tablero(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }

    // ==================================================================
    // Carga de nivel
    // ==================================================================

    /**
     * Lee la disposicion inicial desde un archivo de texto.
     *
     * Busca primero en el sistema de archivos y, si no esta, en el classpath,
     * para que el juego ande igual corriendo desde la carpeta del proyecto o
     * empaquetado en un jar.
     */
    public void cargarNivel(String archivo) {
        List<String> lineas = leerLineas(archivo);
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El nivel esta vacio: " + archivo);
        }
        construir(lineas);
    }

    /** Variante para tests y para niveles embebidos en el codigo. */
    public void cargarDesdeLineas(List<String> lineas) {
        construir(lineas);
    }

    private List<String> leerLineas(String archivo) {
        Path ruta = Path.of(archivo);
        if (Files.exists(ruta)) {
            try {
                return Files.readAllLines(ruta, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer el nivel: " + archivo, e);
            }
        }
        String recurso = archivo.startsWith("/") ? archivo : "/" + archivo;
        try (var entrada = Tablero.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new IllegalArgumentException("No se encontro el nivel: " + archivo);
            }
            try (BufferedReader lector =
                         new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
                List<String> lineas = new ArrayList<>();
                String linea;
                while ((linea = lector.readLine()) != null) {
                    lineas.add(linea);
                }
                return lineas;
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el nivel: " + archivo, e);
        }
    }

    private void construir(List<String> lineas) {
        cajas.clear();
        muros.clear();
        cerrojos.clear();
        casillasPowerUp.clear();
        destinos.clear();
        jugador = null;

        filas = lineas.size();
        columnas = 0;
        for (String linea : lineas) {
            columnas = Math.max(columnas, linea.length());
        }
        celdas = new Celda[filas][columnas];

        for (int f = 0; f < filas; f++) {
            String linea = lineas.get(f);
            for (int c = 0; c < columnas; c++) {
                char ch = c < linea.length() ? linea.charAt(c) : CH_VACIO;
                celdas[f][c] = interpretar(ch, f, c);
            }
        }

        // El formato actual declara un solo grupo de cerrojos por nivel: todos
        // los cerrojos abren todos los muros. La estructura (una lista de muros
        // por cerrojo) ya soporta grupos si el formato los llega a declarar.
        for (CasillaCerrojo cerrojo : cerrojos) {
            for (Muro muro : muros) {
                cerrojo.asociarMuro(muro);
            }
        }

        if (jugador == null) {
            throw new IllegalArgumentException("El nivel no tiene jugador");
        }
    }

    private Celda interpretar(char ch, int fila, int columna) {
        switch (ch) {
            case CH_PARED:
                return new Pared(fila, columna);
            case CH_DESTINO: {
                CasillaDestino destino = new CasillaDestino(fila, columna);
                destinos.add(destino);
                return destino;
            }
            case CH_JUGADOR:
                jugador = new Jugador(fila, columna);
                return new EspacioVacio(fila, columna);
            case CH_JUGADOR_EN_DESTINO: {
                jugador = new Jugador(fila, columna);
                CasillaDestino destino = new CasillaDestino(fila, columna);
                destinos.add(destino);
                return destino;
            }
            case CH_CAJA:
                cajas.add(new CajaNormal(fila, columna));
                return new EspacioVacio(fila, columna);
            case CH_CAJA_EN_DESTINO: {
                cajas.add(new CajaNormal(fila, columna));
                CasillaDestino destino = new CasillaDestino(fila, columna);
                destinos.add(destino);
                return destino;
            }
            case CH_CAJA_FRAGIL:
                cajas.add(new CajaFragil(fila, columna));
                return new EspacioVacio(fila, columna);
            case CH_CAJA_LLAVE:
                cajas.add(new CajaLlave(fila, columna));
                return new EspacioVacio(fila, columna);
            case CH_RESBALADIZO:
                return new TerrenoResbaladizo(fila, columna);
            case CH_CERROJO: {
                CasillaCerrojo cerrojo = new CasillaCerrojo(fila, columna);
                cerrojos.add(cerrojo);
                return cerrojo;
            }
            case CH_MURO_CERRADO: {
                Muro muro = new Muro(fila, columna, false);
                muros.add(muro);
                return muro;
            }
            case CH_MURO_ABIERTO: {
                Muro muro = new Muro(fila, columna, true);
                muros.add(muro);
                return muro;
            }
            case '1':
            case '2':
            case '3':
            case '4': {
                TipoPowerUp tipo = TipoPowerUp.values()[ch - '1'];
                CasillaPowerUp casilla = new CasillaPowerUp(fila, columna, tipo);
                casillasPowerUp.add(casilla);
                return casilla;
            }
            default:
                return new EspacioVacio(fila, columna);
        }
    }

    // ==================================================================
    // Movimiento
    // ==================================================================

    /**
     * Intenta mover al jugador un paso en la direccion dada, resolviendo
     * empujes, deslizamientos, roturas, cerrojos y recoleccion de power-ups.
     *
     * @return que paso, para que el controlador sepa si corresponde apilar un
     *         memento (ver ResultadoMovimiento).
     */
    public ResultadoMovimiento moverJugador(Direccion d) {
        int nf = jugador.getFila() + d.getDeltaFila();
        int nc = jugador.getColumna() + d.getDeltaColumna();
        if (!dentro(nf, nc)) {
            return ResultadoMovimiento.sinMovimiento();
        }

        Celda destino = celdas[nf][nc];
        List<Caja> linea = cajasEnLinea(nf, nc, d);
        boolean huboEmpuje = false;

        if (!linea.isEmpty()) {
            // Hay cajas adelante: solo se puede avanzar empujandolas.
            if (linea.size() > jugador.cajasEmpujables()) {
                return ResultadoMovimiento.sinMovimiento();
            }
            Caja ultima = linea.get(linea.size() - 1);
            int tf = ultima.getFila() + d.getDeltaFila();
            int tc = ultima.getColumna() + d.getDeltaColumna();
            if (!libreParaCaja(tf, tc)) {
                return ResultadoMovimiento.sinMovimiento();
            }
            // La celda donde se para el jugador tiene que ser pisable: una caja
            // nunca se empuja hacia adentro de una pared, pero el jugador podria
            // estar empujando desde afuera de una.
            if (!destino.esTransitable() && !jugador.puedeAtravesar(destino)) {
                return ResultadoMovimiento.sinMovimiento();
            }
            // Se mueven de la mas lejana a la mas cercana para no pisarse.
            for (int i = linea.size() - 1; i >= 0; i--) {
                Caja caja = linea.get(i);
                caja.mover(caja.getFila() + d.getDeltaFila(), caja.getColumna() + d.getDeltaColumna());
            }
            huboEmpuje = true;
            jugador.registrarEmpuje();
            gestorEventos.notificar(new EventoJuego(TipoEvento.EMPUJE, nivel));
            resolverEfectosDeEmpuje(linea, d);
        } else if (!destino.esTransitable() && !jugador.puedeAtravesar(destino)) {
            return ResultadoMovimiento.sinMovimiento();
        }

        boolean atravesando = !destino.esTransitable();
        jugador.mover(d);
        if (atravesando) {
            jugador.notificarAtravesoPared();
        }
        gestorEventos.notificar(new EventoJuego(TipoEvento.MOVIMIENTO, nivel));

        if (!jugador.resisteDeslizamiento()) {
            deslizarJugador(d);
        }

        TipoPowerUp recogido = recogerPowerUpBajoElJugador();
        return new ResultadoMovimiento(true, huboEmpuje, recogido);
    }

    /**
     * Desgaste, deslizamiento y cerrojos de las cajas que se acaban de empujar.
     * El tablero no pregunta de que tipo es cada caja: usa los ganchos
     * polimorficos registrarEmpuje() y alPosicionarseEn().
     */
    private void resolverEfectosDeEmpuje(List<Caja> empujadas, Direccion d) {
        for (Caja caja : empujadas) {
            caja.registrarEmpuje(jugador.rompeFragiles());
            if (caja.estaRota()) {
                // No se saca de la lista: se marca rota para que el undo la pueda
                // devolver. cajaEn() ignora las rotas.
                gestorEventos.notificar(new EventoJuego(TipoEvento.CAJA_ROTA, nivel));
                continue;
            }
            deslizarCaja(caja, d);
            Celda apoyo = celdas[caja.getFila()][caja.getColumna()];
            if (caja.alPosicionarseEn(apoyo)) {
                gestorEventos.notificar(new EventoJuego(TipoEvento.CERROJO_ABIERTO, nivel));
            }
        }
    }

    /**
     * Una caja que entra en terreno resbaladizo sigue avanzando en la misma
     * direccion hasta chocar con algo o hasta pisar un piso que no resbale.
     */
    private void deslizarCaja(Caja caja, Direccion d) {
        while (celdas[caja.getFila()][caja.getColumna()] instanceof TerrenoResbaladizo) {
            int nf = caja.getFila() + d.getDeltaFila();
            int nc = caja.getColumna() + d.getDeltaColumna();
            if (!libreParaCaja(nf, nc)) {
                break;
            }
            caja.mover(nf, nc);
        }
    }

    /**
     * El jugador tambien resbala, salvo que tenga el power-up antideslizante.
     * Usa posicionar() y no mover(): el deslizamiento es parte del mismo
     * movimiento, no cuenta como uno nuevo. Y no empuja cajas: si hay una
     * adelante, el deslizamiento se detiene.
     */
    private void deslizarJugador(Direccion d) {
        while (celdas[jugador.getFila()][jugador.getColumna()] instanceof TerrenoResbaladizo) {
            int nf = jugador.getFila() + d.getDeltaFila();
            int nc = jugador.getColumna() + d.getDeltaColumna();
            if (!dentro(nf, nc) || !celdas[nf][nc].esTransitable() || cajaEn(nf, nc) != null) {
                break;
            }
            jugador.posicionar(nf, nc);
        }
    }

    private TipoPowerUp recogerPowerUpBajoElJugador() {
        Celda actual = celdas[jugador.getFila()][jugador.getColumna()];
        if (!(actual instanceof CasillaPowerUp)) {
            return null;
        }
        TipoPowerUp recogido = ((CasillaPowerUp) actual).recoger();
        if (recogido != null) {
            gestorEventos.notificar(EventoJuego.con(TipoEvento.POWERUP_RECOGIDO, nivel,
                    EventoJuego.CLAVE_POWERUP, recogido));
        }
        return recogido;
    }

    /** Cajas consecutivas a partir de (fila, columna) en la direccion dada. */
    private List<Caja> cajasEnLinea(int fila, int columna, Direccion d) {
        List<Caja> linea = new ArrayList<>();
        int f = fila;
        int c = columna;
        Caja caja = cajaEn(f, c);
        while (caja != null) {
            linea.add(caja);
            f += d.getDeltaFila();
            c += d.getDeltaColumna();
            if (!dentro(f, c)) {
                break;
            }
            caja = cajaEn(f, c);
        }
        return linea;
    }

    /** Una caja puede ocupar la celda solo si es transitable y esta libre. */
    private boolean libreParaCaja(int fila, int columna) {
        return dentro(fila, columna)
                && celdas[fila][columna].esTransitable()
                && cajaEn(fila, columna) == null;
    }

    // ==================================================================
    // Consultas
    // ==================================================================

    public Celda obtenerCelda(int fila, int columna) {
        return dentro(fila, columna) ? celdas[fila][columna] : null;
    }

    /** La caja entera que esta en esa celda, o null. Ignora las rotas. */
    public Caja cajaEn(int fila, int columna) {
        for (Caja caja : cajas) {
            if (!caja.estaRota() && caja.getFila() == fila && caja.getColumna() == columna) {
                return caja;
            }
        }
        return null;
    }

    public boolean dentro(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    /**
     * Victoria cuando todas las casillas destino estan ocupadas por una caja.
     *
     * Se evalua por destino y no por caja porque las cajas llave terminan
     * apoyadas sobre un cerrojo, no sobre un destino, y porque una caja fragil
     * puede haberse roto. Si por romper una caja quedan menos cajas que
     * destinos, el nivel se vuelve irresoluble y hay que reiniciarlo.
     */
    public boolean todasLasCajasEnDestino() {
        if (destinos.isEmpty()) {
            return false;
        }
        for (CasillaDestino destino : destinos) {
            if (cajaEn(destino.getFila(), destino.getColumna()) == null) {
                return false;
            }
        }
        return true;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public List<Caja> getCajas() {
        return List.copyOf(cajas);
    }

    public List<CasillaDestino> getDestinos() {
        return List.copyOf(destinos);
    }

    public IJugador getJugador() {
        return jugador;
    }

    /**
     * Juego reemplaza la referencia cada vez que agrega o quita un power-up:
     * la punta de la cadena de decoradores es otro objeto, y el tablero tiene
     * que preguntarle a la punta, no al jugador crudo. Es la consecuencia
     * directa de haber elegido Decorator.
     */
    public void setJugador(IJugador jugador) {
        this.jugador = jugador;
    }

    // ==================================================================
    // Soporte para el Memento
    // ==================================================================

    public List<Memento.EstadoCaja> capturarCajas() {
        List<Memento.EstadoCaja> estados = new ArrayList<>();
        for (Caja caja : cajas) {
            estados.add(new Memento.EstadoCaja(
                    caja.getFila(), caja.getColumna(), caja.getResistencia(), caja.estaRota()));
        }
        return estados;
    }

    public List<Boolean> capturarMuros() {
        List<Boolean> estados = new ArrayList<>();
        for (Muro muro : muros) {
            estados.add(muro.estaAbierto());
        }
        return estados;
    }

    public List<Boolean> capturarCerrojos() {
        List<Boolean> estados = new ArrayList<>();
        for (CasillaCerrojo cerrojo : cerrojos) {
            estados.add(cerrojo.estaActivado());
        }
        return estados;
    }

    public List<Boolean> capturarPowerUps() {
        List<Boolean> estados = new ArrayList<>();
        for (CasillaPowerUp casilla : casillasPowerUp) {
            estados.add(casilla.estaRecogido());
        }
        return estados;
    }

    /**
     * Repone el estado del tablero desde una foto. Las listas van en el mismo
     * orden en que se capturaron, que es el orden de carga del nivel y no
     * cambia durante la partida.
     */
    public void restaurar(Memento memento) {
        List<Memento.EstadoCaja> estadoCajas = memento.getEstadoCajas();
        for (int i = 0; i < cajas.size() && i < estadoCajas.size(); i++) {
            Memento.EstadoCaja estado = estadoCajas.get(i);
            cajas.get(i).restaurarEstado(
                    estado.getFila(), estado.getColumna(), estado.getResistencia(), estado.estaRota());
        }
        List<Boolean> estadoMuros = memento.getMurosAbiertos();
        for (int i = 0; i < muros.size() && i < estadoMuros.size(); i++) {
            muros.get(i).restaurarEstado(estadoMuros.get(i));
        }
        List<Boolean> estadoCerrojos = memento.getCerrojosActivados();
        for (int i = 0; i < cerrojos.size() && i < estadoCerrojos.size(); i++) {
            cerrojos.get(i).restaurarEstado(estadoCerrojos.get(i));
        }
        List<Boolean> estadoPowerUps = memento.getPowerUpsRecogidos();
        for (int i = 0; i < casillasPowerUp.size() && i < estadoPowerUps.size(); i++) {
            casillasPowerUp.get(i).restaurarEstado(estadoPowerUps.get(i));
        }
    }
}
