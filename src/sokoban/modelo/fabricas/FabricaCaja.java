package sokoban.modelo.fabricas;

import sokoban.modelo.entidades.Caja;

/**
 * Patron Factory Method, lado de las cajas. Creador abstracto: cada fabrica
 * concreta decide que subclase de Caja instancia.
 */
public interface FabricaCaja {

    /** Metodo fabrica: crea la caja ubicada en (fila, columna). */
    Caja crear(int fila, int columna);
}
