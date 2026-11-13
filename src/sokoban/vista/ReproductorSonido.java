package sokoban.vista;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import sokoban.eventos.EventoJuego;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;

/**
 * Observador que le pone sonido a los eventos del juego.
 *
 * Es el ejemplo mas claro de para que sirve el Observer aca: el modelo no sabe
 * que existe el sonido, y agregar o quitar este observador no cambia una linea
 * de las reglas.
 *
 * Carga los .wav de assets/sonidos al arrancar. Si un archivo no esta, el
 * evento simplemente no suena: el juego no se cae por falta de assets.
 */
public class ReproductorSonido implements Observador {

    private static final String CARPETA_SONIDOS = "assets/sonidos/";

    private final Map<TipoEvento, Clip> sonidos = new EnumMap<>(TipoEvento.class);

    public ReproductorSonido() {
        cargar(TipoEvento.MOVIMIENTO, "movimiento.wav");
        cargar(TipoEvento.EMPUJE, "empuje.wav");
        cargar(TipoEvento.CAJA_ROTA, "caja_rota.wav");
        cargar(TipoEvento.CERROJO_ABIERTO, "cerrojo.wav");
        cargar(TipoEvento.UNDO_USADO, "undo.wav");
        cargar(TipoEvento.POWERUP_RECOGIDO, "powerup.wav");
        cargar(TipoEvento.NIVEL_COMPLETADO, "nivel_completado.wav");
        cargar(TipoEvento.LOGRO_DESBLOQUEADO, "logro.wav");
    }

    private void cargar(TipoEvento tipo, String archivo) {
        File ruta = new File(CARPETA_SONIDOS + archivo);
        if (!ruta.exists()) {
            return;
        }
        try (AudioInputStream entrada = AudioSystem.getAudioInputStream(ruta)) {
            Clip clip = AudioSystem.getClip();
            clip.open(entrada);
            sonidos.put(tipo, clip);
        } catch (Exception e) {
            // Un sonido que no se puede abrir no debe impedir jugar.
        }
    }

    @Override
    public void actualizar(EventoJuego e) {
        reproducir(e.getTipo());
    }

    public void reproducir(TipoEvento tipo) {
        Clip clip = sonidos.get(tipo);
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
}
