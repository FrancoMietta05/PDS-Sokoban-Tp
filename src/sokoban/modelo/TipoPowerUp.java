package sokoban.modelo;

/**
 * Tipos de power-up que puede haber tirados en un nivel.
 *
 * Es el intermediario entre la celda que lo contiene y el decorador que lo
 * implementa (decision 6 de la bitacora): CasillaPowerUp.recoger() devuelve
 * un valor de este enum y GestorPowerUps lo traduce al PowerUpDecorator
 * concreto. De esta forma la jerarquia de Celda no depende del subsistema
 * de decoradores, y el caracter leido del archivo .txt mapea directo a un
 * valor de enumeracion.
 */
public enum TipoPowerUp {
    FUERZA("Fuerza"),
    ANTIDESLIZANTE("Antideslizante"),
    FANTASMA("Fantasma"),
    MANOS_DE_ACERO("Manos de acero");

    private final String nombre;

    TipoPowerUp(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
