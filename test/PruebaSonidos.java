import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Verifica que los 8 efectos existan y que Java los pueda abrir.
 *
 * Existe por una trampa concreta: javax.sound.sampled solo lee WAV, AU y AIFF.
 * Los packs de sonido suelen venir en .ogg, y si alguien copia un .ogg a
 * assets/sonidos/ el juego no se cae: ese evento queda mudo y nadie se entera
 * hasta la demo. Esta prueba lo convierte en un error visible.
 */
public class PruebaSonidos {

    private static final String CARPETA = "assets/sonidos/";

    private static final String[] ESPERADOS = {
        "movimiento.wav", "empuje.wav", "caja_rota.wav", "cerrojo.wav",
        "undo.wav", "powerup.wav", "nivel_completado.wav", "logro.wav"
    };

    public static void main(String[] args) {
        int fallas = 0;
        for (String nombre : ESPERADOS) {
            File archivo = new File(CARPETA + nombre);
            if (!archivo.exists()) {
                System.out.println("  FALTA  " + nombre);
                fallas++;
                continue;
            }
            try (AudioInputStream entrada = AudioSystem.getAudioInputStream(archivo)) {
                AudioFormat formato = entrada.getFormat();
                Clip clip = AudioSystem.getClip();
                clip.open(entrada);
                long ms = clip.getMicrosecondLength() / 1000;
                clip.close();
                System.out.printf("  OK      %-22s %5d ms  %.0f Hz  %d canal(es)%n",
                        nombre, ms, formato.getSampleRate(), formato.getChannels());
                if (ms > 3000) {
                    System.out.println("          aviso: dura mas de 3 s, puede resultar molesto");
                }
            } catch (Exception e) {
                System.out.println("  NO ABRE " + nombre + " -> " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
                fallas++;
            }
        }
        System.out.println();
        System.out.println(fallas == 0 ? "TODO OK" : fallas + " FALLAS");
        System.exit(fallas == 0 ? 0 : 1);
    }
}
