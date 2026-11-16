import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;
import sokoban.logros.GestorLogros;
import sokoban.logros.Logro;
import sokoban.modelo.Direccion;
import sokoban.modelo.DirectorNivel;
import sokoban.modelo.Tablero;
import sokoban.modelo.TableroBuilder;
import sokoban.modelo.celdas.Muro;
import sokoban.modelo.entidades.CajaFragil;
import sokoban.modelo.entidades.CajaLlave;
import sokoban.modelo.entidades.CajaNormal;
import sokoban.modelo.entidades.Jugador;
import sokoban.modelo.fabricas.FabricaCajaFragil;
import sokoban.modelo.fabricas.FabricaCajaLlave;
import sokoban.modelo.fabricas.FabricaCajaNormal;
import sokoban.modelo.fabricas.FabricaMuro;
import sokoban.powerups.PowerUpManosDeAcero;
import sokoban.powerups.fabricas.FabricaPowerUpFuerza;
import sokoban.modelo.TipoPowerUp;

/** Prueba de humo sin interfaz grafica. */
public class PruebaSokoban {

    /**
     * Soluciones verificadas de cada nivel, en notacion U/D/L/R.
     *
     * OJO: dependen del contenido de niveles/nivelN.txt. Si se edita un nivel
     * hay que rehacer su solucion o esta prueba falla, y eso es a proposito:
     * su trabajo es garantizar que los niveles que se entregan son resolubles.
     * Un nivel irresoluble descubierto durante la demo es el peor escenario.
     */
    static final String SOLUCION_1 =
            "UUULDLDRRRRRRRRRDRUDLLLLLLLLDRRRRRRRURDLLLLDDLULURRRRRRURDDRDL";
    static final String SOLUCION_2 =
            "LUUURDLDRRRLLLUUUUURDDLDRRRRRRRRDRUULURURRRDDDDD";
    static final String SOLUCION_3 =
            "DDLDRRRLUULURRRRRLLLLULLURRRRRRRRRRRDLLLDDRRRRR";

    /**
     * Una secuencia que resuelve cada nivel (SOK-11). Un nivel nuevo sin su
     * entrada aca hace fallar la bateria. Las de los niveles 4 a 8 las saco
     * Solucionador (BFS), asi que son las mas cortas posibles.
     */
    static final Map<Integer, String> SOLUCIONES = Map.of(
            1, SOLUCION_1,
            2, SOLUCION_2,
            3, SOLUCION_3,
            4, "URRDRRRLLLULLDR",        // Fuerza
            5, "RRLLLU",                 // Antideslizante
            6, "DRURRRRR",               // Fantasma
            7, "RRRRRDDLL",              // Manos de Acero
            8, "URRDDRRRURDD");          // llave + muro + hielo + fragil

    /** Niveles disenados para que no se puedan resolver sin su power-up. */
    static final Map<Integer, TipoPowerUp> POWERUP_OBLIGATORIO = new TreeMap<>(Map.of(
            4, TipoPowerUp.FUERZA,
            5, TipoPowerUp.ANTIDESLIZANTE,
            6, TipoPowerUp.FANTASMA,
            7, TipoPowerUp.MANOS_DE_ACERO));

    static int fallas = 0;

    static void check(String que, boolean ok) {
        System.out.println((ok ? "  OK   " : "  FALLA") + "  " + que);
        if (!ok) fallas++;
    }

    static void mover(Juego j, String secuencia) {
        for (char c : secuencia.toCharArray()) {
            Direccion d = switch (c) {
                case 'U' -> Direccion.ARRIBA;
                case 'D' -> Direccion.ABAJO;
                case 'L' -> Direccion.IZQUIERDA;
                case 'R' -> Direccion.DERECHA;
                default -> null;
            };
            j.manejarEntrada(d);
        }
    }

    public static void main(String[] args) {
        Juego juego = Juego.getInstance();
        GestorEventos eventos = juego.getGestorEventos();
        GestorLogros logros = new GestorLogros(eventos);
        eventos.agregarObservador(logros);

        List<TipoEvento> registro = new ArrayList<>();
        eventos.agregarObservador(new Observador() {
            public void actualizar(EventoJuego e) { registro.add(e.getTipo()); }
        });


        // ---------- NIVEL 1: tres cajas, tres destinos ----------
        System.out.println("NIVEL 1");
        juego.iniciarJuego();
        check("nivel 1 cargado", juego.getNivelActual() == 1);
        check("no hay victoria al arrancar", !juego.verificarVictoria());
        check("el nivel 1 tiene 3 cajas y 3 destinos",
                juego.getTableroActual().getCajas().size() == 3
                        && juego.getTableroActual().getDestinos().size() == 3);
        mover(juego, SOLUCION_1);
        check("nivel 1 resuelto", juego.estaCompletado());
        check("movimientos = 62", juego.getMovimientos() == 62);
        check("empujes = 29", juego.getEmpujes() == 29);
        check("puntaje > 0", juego.calcularPuntaje() > 0);
        check("se emitio NIVEL_COMPLETADO", registro.contains(TipoEvento.NIVEL_COMPLETADO));
        check("logro Primer paso desbloqueado",
                logros.logrosDesbloqueados().stream().anyMatch(l -> l.getNombre().equals("Primer paso")));
        check("logro Pulso firme desbloqueado",
                logros.logrosDesbloqueados().stream().anyMatch(l -> l.getNombre().equals("Pulso firme")));

        // ---------- NIVEL 2: caja llave, cerrojo y muro ----------
        System.out.println("NIVEL 2");
        check("avanza al nivel 2", juego.siguienteNivel());
        check("nivel 2 cargado", juego.getNivelActual() == 2);
        check("el muro (4,8) arranca cerrado",
                !juego.getTableroActual().obtenerCelda(4, 8).esTransitable());
        mover(juego, SOLUCION_2);
        check("se abrio el cerrojo", registro.contains(TipoEvento.CERROJO_ABIERTO));
        check("el muro quedo transitable",
                juego.getTableroActual().obtenerCelda(4, 8).esTransitable());
        check("logro Cerrajero desbloqueado",
                logros.logrosDesbloqueados().stream().anyMatch(l -> l.getNombre().equals("Cerrajero")));
        check("nivel 2 resuelto", juego.estaCompletado());

        // ---------- NIVEL 3: hielo, cerrojo, muro y caja fragil ----------
        System.out.println("NIVEL 3");
        check("avanza al nivel 3", juego.siguienteNivel());
        check("el muro (6,13) arranca cerrado",
                !juego.getTableroActual().obtenerCelda(6, 13).esTransitable());
        mover(juego, SOLUCION_3);
        check("nivel 3 resuelto", juego.estaCompletado());
        check("el muro se abrio con la caja llave",
                juego.getTableroActual().obtenerCelda(6, 13).esTransitable());
        check("la fragil llego al destino sin romperse",
                juego.getTableroActual().cajaEn(6, 16) != null);
        check("la fragil bajo a resistencia 1 tras 2 empujes",
                juego.getTableroActual().cajaEn(6, 16).getResistencia() == 1);
        check("despues del 3 viene el nivel 4", juego.siguienteNivel() && juego.getNivelActual() == 4);

        // ---------- Deslizamiento en terreno resbaladizo ----------
        System.out.println("HIELO");
        juego.cargarNivel(3);
        mover(juego, SOLUCION_3.substring(0, 17));
        check("la caja empujada al hielo se deslizo sola hasta (4,13)",
                juego.getTableroActual().cajaEn(4, 13) != null);
        check("y freno al salir del hielo, no antes",
                juego.getTableroActual().cajaEn(4, 12) == null);

        juego.cargarNivel(3);
        mover(juego, SOLUCION_3.substring(0, 41));
        check("el jugador tambien resbala: bajo de (4,10) a (5,10)",
                juego.getJugador().getFila() == 5 && juego.getJugador().getColumna() == 10);

        // ---------- Deshacer ----------
        System.out.println("UNDO");
        juego.cargarNivel(1);
        mover(juego, SOLUCION_1.substring(0, 15));
        check("se hicieron 15 movimientos", juego.getMovimientos() == 15);
        check("hay historial para deshacer", juego.puedeDeshacer());
        check("undo 1", juego.deshacerMovimiento());
        check("retrocedio exactamente 5 movimientos", juego.getMovimientos() == 10);
        check("quedo 1 uso consecutivo", juego.getHistorial().getUsosConsecutivos() == 1);
        check("se emitio UNDO_USADO", registro.contains(TipoEvento.UNDO_USADO));
        check("undo 2", juego.deshacerMovimiento());
        check("undo 3", juego.deshacerMovimiento());
        check("el cuarto undo consecutivo se rechaza", !juego.deshacerMovimiento());
        juego.reiniciarNivel();
        check("reinicio deja los contadores en cero", juego.getMovimientos() == 0);

        // ---------- Power-ups (Decorator) ----------
        System.out.println("POWER-UPS");
        juego.cargarNivel(3);
        check("jugador crudo empuja 1 caja", juego.getJugador().cajasEmpujables() == 1);
        juego.aplicarPowerUp(TipoPowerUp.FUERZA);
        check("con Fuerza empuja 2", juego.getJugador().cajasEmpujables() == 2);
        check("la descripcion muestra la cadena",
                juego.getJugador().descripcion().contains("Fuerza"));
        juego.aplicarPowerUp(TipoPowerUp.ANTIDESLIZANTE);
        check("resiste deslizamiento", juego.getJugador().resisteDeslizamiento());
        check("sigue empujando 2 (delegacion)", juego.getJugador().cajasEmpujables() == 2);
        juego.aplicarPowerUp(TipoPowerUp.MANOS_DE_ACERO);
        check("rompe fragiles", juego.getJugador().rompeFragiles());

        // el nivel 3 tiene el ANTIDESLIZANTE en (4,5): se recoge al pisarlo
        juego.cargarNivel(3);
        mover(juego, "RR");
        check("recogio el power-up del nivel",
                registro.contains(TipoEvento.POWERUP_RECOGIDO));
        check("quedo con Antideslizante activo", juego.getJugador().resisteDeslizamiento());

        // expiracion: Fuerza dura 15 movimientos
        juego.cargarNivel(3);
        juego.aplicarPowerUp(TipoPowerUp.FUERZA);
        for (int i = 0; i < 16; i++) {
            mover(juego, i % 2 == 0 ? "U" : "D");
        }
        check("la Fuerza expiro y la cadena se desarmo", juego.getJugador().cajasEmpujables() == 1);

        // ---------- Caja fragil, sobre tableros armados por la prueba ----------
        // No se usa un nivel del juego: las fragiles de los niveles viven detras
        // de un muro, y ademas una prueba de regla no deberia romperse porque
        // alguien reacomodo un nivel.
        System.out.println("CAJA FRAGIL");
        List<String> tableroFragil = List.of("#####", "#@F.#", "#####");

        Tablero conJugadorCrudo = new DirectorNivel().construirDesdeLineas(
                tableroFragil, new TableroBuilder(eventos).nivel(9));
        conJugadorCrudo.moverJugador(Direccion.DERECHA);
        check("el jugador crudo no rompe la fragil de un empuje",
                !conJugadorCrudo.getCajas().get(0).estaRota());
        check("pero le baja la resistencia de 3 a 2",
                conJugadorCrudo.getCajas().get(0).getResistencia() == 2);

        Tablero conManosDeAcero = new DirectorNivel().construirDesdeLineas(
                tableroFragil, new TableroBuilder(eventos).nivel(9));
        conManosDeAcero.setJugador(new PowerUpManosDeAcero(conManosDeAcero.getJugador()));
        conManosDeAcero.moverJugador(Direccion.DERECHA);
        check("con Manos de Acero se rompe de un solo empuje",
                conManosDeAcero.getCajas().get(0).estaRota());
        check("la caja rota deja de ocupar la celda",
                conManosDeAcero.cajaEn(1, 3) == null);

        // ---------- undo con power-ups (pendiente 1 de la bitacora) ----------
        System.out.println("UNDO CON POWER-UPS");
        juego.cargarNivel(3);
        mover(juego, "RRUDUD");        // agarra el ANTIDESLIZANTE en el 2do movimiento
        check("tiene el power-up antes del undo", juego.getJugador().resisteDeslizamiento());
        juego.deshacerMovimiento();    // retrocede 5: cae antes de haberlo agarrado
        check("el undo desarmo la cadena de power-ups",
                !juego.getJugador().resisteDeslizamiento());
        check("el power-up volvio al tablero",
                !((sokoban.modelo.celdas.CasillaPowerUp) juego.getTableroActual()
                        .obtenerCelda(4, 5)).estaRecogido());

        // ---------- Patrones: Singleton, Factory Method y Builder ----------
        System.out.println("PATRONES");
        check("Singleton: getInstance devuelve siempre la misma instancia", Juego.getInstance() == juego);
        check("Factory Method: la fabrica de cajas devuelve el tipo que le toca",
                new FabricaCajaFragil().crear(1, 2) instanceof CajaFragil
                        && new FabricaCajaLlave().crear(1, 2) instanceof CajaLlave
                        && new FabricaCajaNormal().crear(1, 2) instanceof CajaNormal);
        check("Factory Method: la fabrica de muros respeta el estado inicial",
                !((Muro) new FabricaMuro(false).crear(0, 0)).estaAbierto()
                        && ((Muro) new FabricaMuro(true).crear(0, 0)).estaAbierto());
        check("Factory Method: la fabrica de power-ups envuelve al jugador",
                new FabricaPowerUpFuerza().crear(new Jugador(0, 0)).cajasEmpujables() == 2);

        Tablero armado = new DirectorNivel().construirDesdeLineas(
                List.of("#####", "#@$.#", "#####"), new TableroBuilder(eventos).nivel(9));
        check("Builder: arma la grilla completa", armado.getFilas() == 3 && armado.getColumnas() == 5);
        check("Builder: ubica jugador, cajas y destinos",
                armado.getJugador().getFila() == 1 && armado.getJugador().getColumna() == 1
                        && armado.getCajas().size() == 1 && armado.getDestinos().size() == 1);
        check("Builder: conserva el numero de nivel", armado.getNivel() == 9);
        boolean sinJugador = false;
        try {
            new DirectorNivel().construirDesdeLineas(List.of("###", "#.#", "###"), new TableroBuilder(eventos));
        } catch (IllegalArgumentException e) {
            sinJugador = true;
        }
        check("Builder: un nivel sin jugador no llega a ser Tablero", sinJugador);

        Tablero conFragiles = new DirectorNivel().construirDesdeLineas(
                List.of("#######", "#@FGH.#", "#######"), new TableroBuilder(eventos));
        check("Nivel: F, G y H declaran fragiles de resistencia 3, 5 y 8",
                conFragiles.getCajas().size() == 3
                        && conFragiles.getCajas().get(0).getResistencia() == 3
                        && conFragiles.getCajas().get(1).getResistencia() == 5
                        && conFragiles.getCajas().get(2).getResistencia() == 8);
        boolean resistenciaInvalida = false;
        try {
            new FabricaCajaFragil(0);
        } catch (IllegalArgumentException e) {
            resistenciaInvalida = true;
        }
        check("Factory Method: una fabrica no admite resistencia cero", resistenciaInvalida);

        Juego anterior = Juego.getInstance();
        Juego.reiniciarInstancia();
        check("Singleton: reiniciarInstancia permite arrancar una partida limpia",
                Juego.getInstance() != anterior);

        // ---------- SOK-14: partida nueva borra los logros ----------
        System.out.println("PARTIDA NUEVA");
        check("habia logros desbloqueados", !logros.logrosDesbloqueados().isEmpty());
        logros.reiniciar();
        check("GestorLogros.reiniciar() los borra todos", logros.logrosDesbloqueados().isEmpty());

        // ---------- SOK-11: cada nivel tiene su solucion verificada ----------
        // Partida limpia y sin observadores: el solucionador hace miles de
        // movimientos y no tiene que ensuciar los logros ni el registro.
        Juego.reiniciarInstancia();
        Juego limpio = Juego.getInstance();
        System.out.println("SOLVABILIDAD");
        int nivel = 1;
        while (limpio.existeNivel(nivel)) {
            String solucion = SOLUCIONES.get(nivel);
            check("nivel " + nivel + " tiene su solucion en SOLUCIONES", solucion != null);
            if (solucion != null) {
                limpio.cargarNivel(nivel);
                mover(limpio, solucion);
                check("nivel " + nivel + " se resuelve con su secuencia", limpio.estaCompletado());
            }
            nivel++;
        }
        check("el juego tiene 8 niveles", nivel - 1 == 8);

        // ---------- SOK-06 a SOK-09: el power-up es obligatorio ----------
        // El solucionador recorre TODOS los estados alcanzables sin power-ups:
        // que devuelva null es la demostracion de que el nivel no sale sin el.
        System.out.println("POWER-UPS OBLIGATORIOS");
        for (Map.Entry<Integer, TipoPowerUp> e : POWERUP_OBLIGATORIO.entrySet()) {
            check("nivel " + e.getKey() + " es irresoluble sin " + e.getValue().getNombre(),
                    Solucionador.resolver(limpio, e.getKey(), true) == null);
            check("nivel " + e.getKey() + " tiene solucion usando " + e.getValue().getNombre(),
                    Solucionador.resolver(limpio, e.getKey(), false) != null);
        }

        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : (fallas + " FALLAS"));
        System.exit(fallas == 0 ? 0 : 1);
    }
}
