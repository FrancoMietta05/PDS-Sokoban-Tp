package sokoban.modelo.fabricas;

import sokoban.modelo.celdas.Celda;

/**
 * Patron Factory Method, lado de las celdas. Es el creador abstracto: declara
 * el metodo fabrica crear() y cada FabricaXxx concreta decide que subclase de
 * Celda instancia.
 *
 * Quien necesita una celda (DirectorNivel) trabaja contra esta interfaz y no
 * conoce las clases concretas: sumar un tipo de celda nuevo es sumar una
 * fabrica y registrarla, sin tocar el codigo que arma el tablero.
 */
public interface FabricaCelda {

    /** Metodo fabrica: crea la celda ubicada en (fila, columna). */
    Celda crear(int fila, int columna);
}
