package sokoban.eventos;

/**
 * Catalogo de eventos que viajan por el Observer.
 *
 * NIVEL_INICIADO es una adicion respecto del diagrama: los logros que llevan
 * estado por nivel (por ejemplo "terminar sin usar undo") necesitan una senal
 * de arranque de nivel para resetearse. Emitirla como evento evita que Juego
 * tenga que conocer a GestorLogros para avisarle.
 */
public enum TipoEvento {
    NIVEL_INICIADO,
    MOVIMIENTO,
    EMPUJE,
    CAJA_ROTA,
    CERROJO_ABIERTO,
    UNDO_USADO,
    POWERUP_RECOGIDO,
    NIVEL_COMPLETADO,
    LOGRO_DESBLOQUEADO
}
