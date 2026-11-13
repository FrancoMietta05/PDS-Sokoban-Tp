package sokoban.modelo.entidades;

import sokoban.modelo.Direccion;
import sokoban.modelo.celdas.Celda;

/**
 * Las preguntas que el tablero le hace al jugador antes de resolver un
 * movimiento, mas los datos que el controlador y el HUD necesitan de el.
 *
 * Existe para que el Decorator cierre (decision 2 de la bitacora): decorador
 * y decorado tienen que compartir un tipo, y heredar de Jugador haria que cada
 * capa arrastrara una copia del estado. Juego y Tablero declaran su campo con
 * este tipo, nunca con Jugador.
 *
 * Los cuatro metodos del medio -cajasEmpujables, puedeAtravesar,
 * resisteDeslizamiento, rompeFragiles- son exactamente las reglas del juego
 * que un power-up puede modificar. Cada decorador concreto pisa uno y delega
 * el resto.
 *
 * ADICIONES RESPECTO DEL DIAGRAMA, todas forzadas por el Decorator (si el
 * campo se declara IJugador, todo lo que se necesite del jugador tiene que
 * pasar por esta interfaz):
 *   - getMovimientos() / getEmpujes() / registrarEmpuje(): el HUD los muestra
 *     y el Memento los guarda, y los contadores viven en Jugador.
 *   - posicionar() y restaurarContadores(): restaurar un memento y el
 *     deslizamiento reubican al jugador sin que eso cuente como movimiento.
 *   - notificarAtravesoPared(): aviso de que una habilidad se ejercio, para
 *     que puedeAtravesar() siga siendo una consulta sin efectos.
 */
public interface IJugador {

    void mover(Direccion d);

    int getFila();

    int getColumna();

    /** Cuantas cajas en linea puede empujar de una vez. Normalmente 1. */
    int cajasEmpujables();

    /** Si puede pararse en una celda que no es transitable por las reglas normales. */
    boolean puedeAtravesar(Celda c);

    /** Si no se desliza al pisar terreno resbaladizo. */
    boolean resisteDeslizamiento();

    /** Si rompe una caja fragil de un solo empuje. */
    boolean rompeFragiles();

    /** Texto para el HUD: el jugador base o la cadena de power-ups activos. */
    String descripcion();

    int getMovimientos();

    int getEmpujes();

    void registrarEmpuje();

    /** Reubica sin contar movimiento (deslizamiento, restauracion de memento). */
    void posicionar(int fila, int columna);

    /** Aviso de que el jugador acaba de atravesar una celda no transitable. */
    void notificarAtravesoPared();

    /** Repone los contadores guardados en un memento. */
    void restaurarContadores(int movimientos, int empujes);
}
