import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Genera los 8 efectos de sonido del juego por sintesis (SOK-04).
 *
 * No se descargo ningun audio: cada .wav sale de sumar ondas y ruido con
 * envolventes, calculados aca. Por eso los sonidos son obra propia del equipo
 * y se publican como dominio publico (CC0), igual que los sprites de Kenney.
 *
 * El ruido usa una semilla fija: correr el generador de nuevo produce
 * exactamente los mismos archivos.
 *
 * Uso, desde la raiz del proyecto:
 *   javac -d out herramientas/GeneradorSonidos.java
 *   java -cp out GeneradorSonidos
 */
public final class GeneradorSonidos {

    private static final float FRECUENCIA_MUESTREO = 22050f;
    private static final String CARPETA = "assets/sonidos/";

    private GeneradorSonidos() {
    }

    public static void main(String[] args) throws IOException {
        new File(CARPETA).mkdirs();
        guardar("movimiento.wav", movimiento());
        guardar("empuje.wav", empuje());
        guardar("caja_rota.wav", cajaRota());
        guardar("cerrojo.wav", cerrojo());
        guardar("undo.wav", undo());
        guardar("powerup.wav", powerUp());
        guardar("nivel_completado.wav", nivelCompletado());
        guardar("logro.wav", logro());
    }

    // ------------------------------------------------------------------
    // Los sonidos
    // ------------------------------------------------------------------

    /** Paso: un golpecito grave y muy corto. Suena en cada movimiento, asi que va bajito. */
    private static double[] movimiento() {
        double[] s = muestras(0.045);
        for (int i = 0; i < s.length; i++) {
            double t = tiempo(i);
            s[i] = Math.sin(2 * Math.PI * 160 * t) * decaer(t, 0.012);
        }
        return volumen(s, 0.18);
    }

    /** Empuje: arrastre de madera, ruido filtrado con un tono grave que cae. */
    private static double[] empuje() {
        Random r = new Random(2);
        double[] s = muestras(0.14);
        double filtrado = 0;
        for (int i = 0; i < s.length; i++) {
            double t = tiempo(i);
            filtrado += (r.nextGaussian() - filtrado) * 0.08;
            double tono = Math.sin(2 * Math.PI * (120 - 200 * t) * t);
            s[i] = (filtrado * 2.5 + tono * 0.6) * decaer(t, 0.05);
        }
        return volumen(s, 0.55);
    }

    /** Caja rota: crujido de ruido con chasquidos que se apagan. */
    private static double[] cajaRota() {
        Random r = new Random(3);
        double[] s = muestras(0.35);
        double filtrado = 0;
        for (int i = 0; i < s.length; i++) {
            double t = tiempo(i);
            filtrado += (r.nextGaussian() - filtrado) * 0.35;
            double chasquido = r.nextDouble() < 0.004 ? r.nextGaussian() * 3 : 0;
            s[i] = (filtrado + chasquido) * decaer(t, 0.09);
        }
        return volumen(s, 0.7);
    }

    /** Cerrojo: dos clics metalicos y un tono que sube. */
    private static double[] cerrojo() {
        double[] s = muestras(0.45);
        for (int i = 0; i < s.length; i++) {
            double t = tiempo(i);
            double clic1 = Math.sin(2 * Math.PI * 2400 * t) * decaer(t, 0.006);
            double clic2 = t > 0.08 ? Math.sin(2 * Math.PI * 1800 * t) * decaer(t - 0.08, 0.008) : 0;
            double tono = t > 0.15
                    ? Math.sin(2 * Math.PI * (440 + 500 * (t - 0.15)) * t) * envolvente(t - 0.15, 0.3)
                    : 0;
            s[i] = clic1 + clic2 + tono * 0.5;
        }
        return volumen(s, 0.6);
    }

    /** Deshacer: barrido descendente, como rebobinar. */
    private static double[] undo() {
        double[] s = muestras(0.22);
        double fase = 0;
        for (int i = 0; i < s.length; i++) {
            double t = tiempo(i);
            fase += 2 * Math.PI * (700 - 2200 * t) / FRECUENCIA_MUESTREO;
            s[i] = cuadrada(fase) * 0.5 * envolvente(t, 0.22);
        }
        return volumen(s, 0.4);
    }

    /** Power-up: arpegio ascendente rapido (do, mi, sol, do). */
    private static double[] powerUp() {
        return volumen(arpegio(new double[]{523.25, 659.25, 783.99, 1046.5}, 0.07, 0.18), 0.5);
    }

    /** Nivel completado: fanfarria corta en mayor que termina en un acorde. */
    private static double[] nivelCompletado() {
        double[] notas = {392.0, 523.25, 659.25, 783.99};
        double[] s = muestras(1.1);
        for (int n = 0; n < notas.length; n++) {
            double inicio = n * 0.12;
            for (int i = 0; i < s.length; i++) {
                double t = tiempo(i) - inicio;
                if (t >= 0) {
                    // Las notas se van sumando: al final suenan las cuatro juntas.
                    s[i] += (Math.sin(2 * Math.PI * notas[n] * t)
                            + 0.3 * Math.sin(2 * Math.PI * notas[n] * 2 * t))
                            * envolvente(t, 1.1 - inicio);
                }
            }
        }
        return volumen(s, 0.6);
    }

    /** Logro: dos campanadas brillantes. */
    private static double[] logro() {
        double[] s = muestras(0.6);
        double[] notas = {1318.5, 1760.0};
        for (int n = 0; n < notas.length; n++) {
            double inicio = n * 0.13;
            for (int i = 0; i < s.length; i++) {
                double t = tiempo(i) - inicio;
                if (t >= 0) {
                    s[i] += (Math.sin(2 * Math.PI * notas[n] * t)
                            + 0.4 * Math.sin(2 * Math.PI * notas[n] * 2.76 * t) * decaer(t, 0.05))
                            * decaer(t, 0.18);
                }
            }
        }
        return volumen(s, 0.5);
    }

    // ------------------------------------------------------------------
    // Utilidades de sintesis
    // ------------------------------------------------------------------

    private static double[] arpegio(double[] notas, double separacion, double largoNota) {
        double[] s = muestras(separacion * (notas.length - 1) + largoNota);
        for (int n = 0; n < notas.length; n++) {
            double inicio = n * separacion;
            for (int i = 0; i < s.length; i++) {
                double t = tiempo(i) - inicio;
                if (t >= 0 && t < largoNota) {
                    s[i] += cuadrada(2 * Math.PI * notas[n] * t) * 0.4 * envolvente(t, largoNota);
                }
            }
        }
        return s;
    }

    private static double[] muestras(double segundos) {
        return new double[(int) (segundos * FRECUENCIA_MUESTREO)];
    }

    private static double tiempo(int muestra) {
        return muestra / FRECUENCIA_MUESTREO;
    }

    /** Caida exponencial: a los 'constante' segundos el sonido bajo a un 37%. */
    private static double decaer(double t, double constante) {
        return Math.exp(-t / constante);
    }

    /** Ataque corto y caida lineal hasta cero al final: evita clics al cortar. */
    private static double envolvente(double t, double largo) {
        if (t < 0 || t >= largo) {
            return 0;
        }
        double ataque = Math.min(1, t / 0.005);
        return ataque * (1 - t / largo);
    }

    /** Onda cuadrada suavizada: suena "a videojuego" sin ser tan aspera. */
    private static double cuadrada(double fase) {
        return Math.tanh(4 * Math.sin(fase));
    }

    /** Normaliza al pico pedido (0..1) y aplica un fade-out de 5 ms al final. */
    private static double[] volumen(double[] s, double pico) {
        double max = 0;
        for (double v : s) {
            max = Math.max(max, Math.abs(v));
        }
        int fade = (int) (0.005 * FRECUENCIA_MUESTREO);
        for (int i = 0; i < s.length; i++) {
            s[i] = max == 0 ? 0 : s[i] / max * pico;
            int restantes = s.length - 1 - i;
            if (restantes < fade) {
                s[i] *= restantes / (double) fade;
            }
        }
        return s;
    }

    /** Escribe las muestras como WAV PCM de 16 bits, mono. */
    private static void guardar(String nombre, double[] s) throws IOException {
        byte[] bytes = new byte[s.length * 2];
        for (int i = 0; i < s.length; i++) {
            int v = (int) Math.round(Math.max(-1, Math.min(1, s[i])) * Short.MAX_VALUE);
            bytes[2 * i] = (byte) v;
            bytes[2 * i + 1] = (byte) (v >> 8);
        }
        AudioFormat formato = new AudioFormat(FRECUENCIA_MUESTREO, 16, 1, true, false);
        try (AudioInputStream entrada =
                     new AudioInputStream(new ByteArrayInputStream(bytes), formato, s.length)) {
            AudioSystem.write(entrada, AudioFileFormat.Type.WAVE, new File(CARPETA + nombre));
        }
        System.out.println("  " + CARPETA + nombre + "  (" + (s.length * 1000 / (int) FRECUENCIA_MUESTREO) + " ms)");
    }
}
