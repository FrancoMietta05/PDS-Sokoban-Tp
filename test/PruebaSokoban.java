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
import sokoban.powerups.fabricas.FabricaPowerUpFuerza;
import sokoban.modelo.TipoPowerUp;

/** Prueba de humo sin interfaz grafica. */
public class PruebaSokoban {

    static int fallas = 0;

    /**
     * Una secuencia que resuelve cada nivel (SOK-11). Un nivel nuevo sin su
     * entrada aca hace fallar la bateria. Las saca Solucionador (BFS), asi que
     * son las mas cortas posibles.
     */
    static final Map<Integer, String> SOLUCIONES = Map.of(
            1, "LURRR",
            2, "LURRURDDDRDRUULURRR",
            3, "LDRRULLURRRRR",
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


        // ---------- NIVEL 1 ----------
        System.out.println("NIVEL 1");
        juego.iniciarJuego();
        check("nivel 1 cargado", juego.getNivelActual() == 1);
        check("no hay victoria al arrancar", !juego.verificarVictoria());
        mover(juego, "LURRR");
        check("nivel 1 resuelto", juego.estaCompletado());
        check("movimientos = 5", juego.getMovimientos() == 5);
        check("empujes = 3", juego.getEmpujes() == 3);
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
        // Jugador (3,3) -> (1,3) rodeando, empuja L hasta (5,3), despues a (5,5)
        mover(juego, "RRRRUULLLL");
        mover(juego, "DDD");          // L: (2,3) -> (5,3)
        mover(juego, "LD");           // jugador a (5,2)
        mover(juego, "RR");           // L: (5,3) -> (5,5) = cerrojo
        check("se abrio el cerrojo", registro.contains(TipoEvento.CERROJO_ABIERTO));
        check("logro Cerrajero desbloqueado",
                logros.logrosDesbloqueados().stream().anyMatch(l -> l.getNombre().equals("Cerrajero")));
        check("el muro quedo transitable", juego.getTableroActual().obtenerCelda(2, 8).esTransitable());
        mover(juego, "UURRRDDL");     // jugador hasta (5,6)
        mover(juego, "UU");           // caja: (4,6) -> (2,6)
        mover(juego, "LU");           // jugador a (2,5)
        mover(juego, "RRRR");         // caja: (2,6) -> (2,10) = destino
        check("nivel 2 resuelto", juego.estaCompletado());

        // ---------- NIVEL 3: fragil, resbaladizo y power-up ----------
        System.out.println("NIVEL 3");
        check("avanza al nivel 3", juego.siguienteNivel());
        mover(juego, "LD");           // jugador a (4,2)
        mover(juego, "RR");           // F: (4,3) -> (4,5) = destino
        check("la fragil no se rompio", juego.getTableroActual().cajaEn(4, 5) != null);
        check("resistencia bajo a 1", juego.getTableroActual().cajaEn(4, 5).getResistencia() == 1);

        // undo: 3 usos consecutivos de 5 movimientos, el cuarto no
        int movAntes = juego.getMovimientos();
        check("hay historial para deshacer", juego.puedeDeshacer());
        check("undo 1", juego.deshacerMovimiento());
        check("retrocedio movimientos", juego.getMovimientos() < movAntes);
        check("undo 2 ya sin historial o permitido",
                juego.getHistorial().getUsosConsecutivos() == 1);
        check("se emitio UNDO_USADO", registro.contains(TipoEvento.UNDO_USADO));

        // rehacer el tramo y resolver el nivel
        juego.reiniciarNivel();
        check("reinicio deja los contadores en cero", juego.getMovimientos() == 0);
        mover(juego, "LD");
        mover(juego, "RR");
        mover(juego, "ULLU");         // jugador a (2,2)
        mover(juego, "RRR");          // caja empujada al hielo, se desliza a (2,9)
        check("la caja se deslizo hasta (2,9)", juego.getTableroActual().cajaEn(2, 9) != null);
        mover(juego, "R");            // el jugador tambien resbala y queda en (2,8)
        check("el jugador resbalo hasta (2,8)",
                juego.getJugador().getFila() == 2 && juego.getJugador().getColumna() == 8);
        mover(juego, "R");            // caja -> (2,10) destino
        check("nivel 3 resuelto", juego.estaCompletado());
        check("despues del 3 viene el nivel 4", juego.siguienteNivel() && juego.getNivelActual() == 4);

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

        // el power-up del nivel 3 esta en (3,10): se recoge al pisarlo
        juego.cargarNivel(3);
        mover(juego, "RRRRRRR");
        check("recogio el power-up del nivel",
                registro.contains(TipoEvento.POWERUP_RECOGIDO));
        check("quedo con Fuerza activa", juego.getJugador().cajasEmpujables() == 2);

        // expiracion: Fuerza dura 15 movimientos
        juego.cargarNivel(3);
        juego.aplicarPowerUp(TipoPowerUp.FUERZA);
        for (int i = 0; i < 16; i++) {
            mover(juego, i % 2 == 0 ? "U" : "D");
        }
        check("la Fuerza expiro y la cadena se desarmo", juego.getJugador().cajasEmpujables() == 1);

        // manos de acero rompe la fragil de un empuje
        juego.cargarNivel(3);
        juego.aplicarPowerUp(TipoPowerUp.MANOS_DE_ACERO);
        mover(juego, "LDR");
        check("la fragil se rompio de un empuje", registro.contains(TipoEvento.CAJA_ROTA));
        check("la caja rota ya no ocupa celda", juego.getTableroActual().cajaEn(4, 4) == null);

        // ---------- undo con power-ups (pendiente 1 de la bitacora) ----------
        System.out.println("UNDO CON POWER-UPS");
        juego.cargarNivel(3);
        mover(juego, "RRRRRRR");       // agarra FUERZA en (3,10)
        check("tiene Fuerza antes del undo", juego.getJugador().cajasEmpujables() == 2);
        juego.deshacerMovimiento();    // vuelve 5 movimientos: antes de agarrarlo
        check("el undo desarmo la cadena de power-ups",
                juego.getJugador().cajasEmpujables() == 1);
        check("el power-up volvio al tablero",
                !((sokoban.modelo.celdas.CasillaPowerUp) juego.getTableroActual()
                        .obtenerCelda(3, 10)).estaRecogido());

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

        Juego anterior = Juego.getInstance();
        Juego.reiniciarInstancia();
        check("Singleton: reiniciarInstancia permite arrancar una partida limpia",
                Juego.getInstance() != anterior);

        // ---------- SOK-14: partida nueva borra los logros ----------
        System.out.println("PARTIDA NUEVA");
        check("habia logros desbloqueados", !logros.logrosDesbloqueados().isEmpty());
        logros.reiniciar();
        check("GestorLogros.reiniciar() los borra todos", logros.logrosDesbloqueados().isEmpty());

        // ---------- SOK-12: resistencia de la fragil desde el .txt ----------
        System.out.println("RESISTENCIAS");
        Tablero fragiles = new DirectorNivel().construirDesdeLineas(
                List.of("#######", "#@FGH.#", "#######"), new TableroBuilder(eventos));
        check("F arranca con resistencia 3", fragiles.cajaEn(1, 2).getResistencia() == 3);
        check("G arranca con resistencia 5", fragiles.cajaEn(1, 3).getResistencia() == 5);
        check("H arranca con resistencia 8", fragiles.cajaEn(1, 4).getResistencia() == 8);

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
