package sokoban.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sokoban.modelo.entidades.Jugador;
import sokoban.modelo.fabricas.FabricaCaja;
import sokoban.modelo.fabricas.FabricaCajaFragil;
import sokoban.modelo.fabricas.FabricaCajaLlave;
import sokoban.modelo.fabricas.FabricaCajaNormal;
import sokoban.modelo.fabricas.FabricaCasillaPowerUp;
import sokoban.modelo.fabricas.FabricaCelda;
import sokoban.modelo.fabricas.FabricaCerrojo;
import sokoban.modelo.fabricas.FabricaDestino;
import sokoban.modelo.fabricas.FabricaEspacioVacio;
import sokoban.modelo.fabricas.FabricaMuro;
import sokoban.modelo.fabricas.FabricaPared;
import sokoban.modelo.fabricas.FabricaResbaladizo;

/**
 * Director del patron Builder, y unico lugar que conoce el formato de los
 * archivos de nivel. Lee el texto, interpreta cada caracter y le va pidiendo
 * al TableroBuilder los pasos en orden.
 *
 * Para crear cada elemento no usa new: busca la FabricaCelda o FabricaCaja
 * registrada para el caracter (patron Factory Method). Un caracter puede
 * producir mas de un elemento -'+' es un destino con el jugador encima, '$' es
 * una caja sobre piso vacio-, por eso hay tres tablas independientes: la de
 * celdas, la de cajas y el conjunto de marcas de jugador.
 */
public class DirectorNivel {

    /** Mapa de caracteres del archivo .txt a elementos del tablero. */
    public static final char CH_PARED = '#';
    public static final char CH_VACIO = ' ';
    public static final char CH_DESTINO = '.';
    public static final char CH_JUGADOR = '@';
    public static final char CH_JUGADOR_EN_DESTINO = '+';
    public static final char CH_CAJA = '$';
    public static final char CH_CAJA_EN_DESTINO = '*';
    public static final char CH_CAJA_FRAGIL = 'F';
    /** Fragil reforzada: aguanta mas empujes que la comun (SOK-12). */
    public static final char CH_CAJA_FRAGIL_MEDIA = 'G';
    public static final char CH_CAJA_FRAGIL_DURA = 'H';
    public static final int RESISTENCIA_MEDIA = 5;
    public static final int RESISTENCIA_DURA = 8;
    public static final char CH_CAJA_LLAVE = 'L';
    public static final char CH_RESBALADIZO = '~';
    public static final char CH_CERROJO = 'c';
    public static final char CH_MURO_CERRADO = 'M';
    public static final char CH_MURO_ABIERTO = 'm';

    private final Map<Character, FabricaCelda> fabricasCelda = new HashMap<>();
    private final Map<Character, FabricaCaja> fabricasCaja = new HashMap<>();
    private final Set<Character> marcasJugador = Set.of(CH_JUGADOR, CH_JUGADOR_EN_DESTINO);
    private final FabricaCelda fabricaPorDefecto = new FabricaEspacioVacio();

    public DirectorNivel() {
        FabricaCelda vacio = fabricaPorDefecto;
        FabricaCelda destino = new FabricaDestino();

        fabricasCelda.put(CH_PARED, new FabricaPared());
        fabricasCelda.put(CH_VACIO, vacio);
        fabricasCelda.put(CH_DESTINO, destino);
        fabricasCelda.put(CH_RESBALADIZO, new FabricaResbaladizo());
        fabricasCelda.put(CH_CERROJO, new FabricaCerrojo());
        fabricasCelda.put(CH_MURO_CERRADO, new FabricaMuro(false));
        fabricasCelda.put(CH_MURO_ABIERTO, new FabricaMuro(true));
        // Los elementos que se paran sobre una celda declaran cual es el piso.
        fabricasCelda.put(CH_JUGADOR, vacio);
        fabricasCelda.put(CH_JUGADOR_EN_DESTINO, destino);
        fabricasCelda.put(CH_CAJA, vacio);
        fabricasCelda.put(CH_CAJA_EN_DESTINO, destino);
        fabricasCelda.put(CH_CAJA_FRAGIL, vacio);
        fabricasCelda.put(CH_CAJA_FRAGIL_MEDIA, vacio);
        fabricasCelda.put(CH_CAJA_FRAGIL_DURA, vacio);
        fabricasCelda.put(CH_CAJA_LLAVE, vacio);
        // '1'..'4' siguen el orden del enum TipoPowerUp.
        TipoPowerUp[] tipos = TipoPowerUp.values();
        for (int i = 0; i < tipos.length; i++) {
            fabricasCelda.put((char) ('1' + i), new FabricaCasillaPowerUp(tipos[i]));
        }

        fabricasCaja.put(CH_CAJA, new FabricaCajaNormal());
        fabricasCaja.put(CH_CAJA_EN_DESTINO, new FabricaCajaNormal());
        fabricasCaja.put(CH_CAJA_FRAGIL, new FabricaCajaFragil());
        fabricasCaja.put(CH_CAJA_FRAGIL_MEDIA, new FabricaCajaFragil(RESISTENCIA_MEDIA));
        fabricasCaja.put(CH_CAJA_FRAGIL_DURA, new FabricaCajaFragil(RESISTENCIA_DURA));
        fabricasCaja.put(CH_CAJA_LLAVE, new FabricaCajaLlave());
    }

    /**
     * Lee la disposicion inicial desde un archivo de texto y arma el tablero.
     *
     * Busca primero en el sistema de archivos y, si no esta, en el classpath,
     * para que el juego ande igual corriendo desde la carpeta del proyecto o
     * empaquetado en un jar.
     */
    public Tablero construir(String archivo, TableroBuilder builder) {
        List<String> lineas = leerLineas(archivo);
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El nivel esta vacio: " + archivo);
        }
        return construirDesdeLineas(lineas, builder);
    }

    /** Variante para tests y para niveles embebidos en el codigo. */
    public Tablero construirDesdeLineas(List<String> lineas, TableroBuilder builder) {
        int filas = lineas.size();
        int columnas = 0;
        for (String linea : lineas) {
            columnas = Math.max(columnas, linea.length());
        }

        builder.dimensiones(filas, columnas);
        for (int f = 0; f < filas; f++) {
            String linea = lineas.get(f);
            for (int c = 0; c < columnas; c++) {
                char ch = c < linea.length() ? linea.charAt(c) : CH_VACIO;
                interpretar(ch, f, c, builder);
            }
        }
        return builder.build();
    }

    private void interpretar(char ch, int fila, int columna, TableroBuilder builder) {
        builder.agregarCelda(fabricasCelda.getOrDefault(ch, fabricaPorDefecto).crear(fila, columna));

        FabricaCaja fabricaCaja = fabricasCaja.get(ch);
        if (fabricaCaja != null) {
            builder.agregarCaja(fabricaCaja.crear(fila, columna));
        }
        if (marcasJugador.contains(ch)) {
            builder.colocarJugador(new Jugador(fila, columna));
        }
    }

    private List<String> leerLineas(String archivo) {
        Path ruta = Path.of(archivo);
        if (Files.exists(ruta)) {
            try {
                return Files.readAllLines(ruta, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer el nivel: " + archivo, e);
            }
        }
        String recurso = archivo.startsWith("/") ? archivo : "/" + archivo;
        try (var entrada = DirectorNivel.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new IllegalArgumentException("No se encontro el nivel: " + archivo);
            }
            try (BufferedReader lector =
                         new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
                List<String> lineas = new ArrayList<>();
                String linea;
                while ((linea = lector.readLine()) != null) {
                    lineas.add(linea);
                }
                return lineas;
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el nivel: " + archivo, e);
        }
    }
}
