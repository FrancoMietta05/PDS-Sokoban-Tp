package sokoban.vista;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;
import sokoban.logros.GestorLogros;
import sokoban.modelo.Direccion;

/**
 * Ventana del juego. Arma el cableado de toda la aplicacion: crea el
 * controlador y los servicios del modelo, crea los paneles y suscribe cada
 * observador al GestorEventos.
 *
 * Es el unico lugar donde se traduce teclado a modelo: un KeyEvent se convierte
 * en Direccion antes de llegar al controlador. Por eso ninguna clase del
 * modelo importa nada de java.awt ni de javax.swing, que es lo que hace que el
 * modelo sea testeable y reusable sin la interfaz (MVC).
 */
public class VentanaPrincipal extends JFrame implements Observador {

    // Juego es Singleton: la ventana no lo crea, lo pide. El orden importa:
    // el resto de los campos depende de que juego ya este inicializado.
    private final Juego juego = Juego.getInstance();
    private final GestorEventos gestorEventos = juego.getGestorEventos();
    private final GestorLogros gestorLogros = new GestorLogros(gestorEventos);

    private final PanelTablero panelTablero = new PanelTablero(juego);
    private final PanelHUD panelHUD = new PanelHUD(juego);
    private final PanelNotificaciones panelNotificaciones = new PanelNotificaciones();
    private final ReproductorSonido reproductorSonido = new ReproductorSonido();
    private final PanelResumen panelResumen = new PanelResumen();
    private final PanelFinal panelFinal = new PanelFinal();

    public VentanaPrincipal() {
        super("Sokoban - PDS");

        gestorEventos.agregarObservador(gestorLogros);
        gestorEventos.agregarObservador(panelTablero);
        gestorEventos.agregarObservador(panelHUD);
        gestorEventos.agregarObservador(panelNotificaciones);
        gestorEventos.agregarObservador(reproductorSonido);
        gestorEventos.agregarObservador(this);

        // Cada componente va en una zona del fondo, en pixeles de Fondo.jpg
        // (2752 x 1536): el tablero en el hueco del marco, el cartel del HUD
        // colgando de las cadenas y los botones sobre los recuadros de abajo.
        PanelFondo fondo = new PanelFondo();
        fondo.agregar(panelTablero, 648, 366, 1464, 816);
        fondo.agregar(panelHUD, 802, 105, 1156, 150);
        fondo.agregarCentrado(panelHUD.getBotonDeshacer(), 404, 1361, 320, 120);
        fondo.agregarCentrado(panelHUD.getBotonReiniciar(), 2351, 1361, 320, 120);
        fondo.agregar(panelNotificaciones, 620, 1318, 1500, 64);
        setContentPane(fondo);
        setGlassPane(panelResumen);

        configurarTeclado();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        juego.iniciarJuego();
        pack();
        setLocationRelativeTo(null);
        panelTablero.requestFocusInWindow();
    }

    /**
     * Conecta el teclado con el controlador.
     *
     * Se usan key bindings y no un KeyListener. Un KeyListener solo recibe
     * teclas si su componente tiene el foco, y alcanzaba con hacer clic en
     * "Deshacer" o "Reiniciar" para que el foco se fuera al boton y el juego
     * dejara de responder a las flechas. WHEN_IN_FOCUSED_WINDOW hace que las
     * teclas lleguen mientras la ventana este activa, sin importar que
     * componente tenga el foco en ese momento.
     *
     * Es tambien el unico lugar donde el teclado se traduce a modelo: de aca
     * sale una Direccion, nunca un KeyEvent. Por eso ninguna clase del modelo
     * ni del controlador depende de Swing.
     */
    private void configurarTeclado() {
        InputMap entrada = panelTablero.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = panelTablero.getActionMap();

        vincular(entrada, acciones, "UP", "mover.arriba",
                () -> juego.manejarEntrada(Direccion.ARRIBA));
        vincular(entrada, acciones, "DOWN", "mover.abajo",
                () -> juego.manejarEntrada(Direccion.ABAJO));
        vincular(entrada, acciones, "LEFT", "mover.izquierda",
                () -> juego.manejarEntrada(Direccion.IZQUIERDA));
        vincular(entrada, acciones, "RIGHT", "mover.derecha",
                () -> juego.manejarEntrada(Direccion.DERECHA));
        vincular(entrada, acciones, "Z", "deshacer", juego::deshacerMovimiento);
        vincular(entrada, acciones, "R", "reiniciar", juego::reiniciarNivel);
    }

    private void vincular(InputMap entrada, ActionMap acciones,
                          String tecla, String nombre, Runnable accion) {
        entrada.put(KeyStroke.getKeyStroke(tecla), nombre);
        acciones.put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    /**
     * La ventana tambien es observador: al completarse un nivel muestra el
     * resumen de desempeno que pide el enunciado y pasa al siguiente.
     */
    @Override
    public void actualizar(EventoJuego e) {
        if (e.getTipo() != TipoEvento.NIVEL_COMPLETADO) {
            return;
        }
        // Se posterga para que el tablero ya haya repintado la jugada final.
        SwingUtilities.invokeLater(() -> mostrarResumen(e));
    }

    private void mostrarResumen(EventoJuego e) {
        setGlassPane(panelResumen);
        panelResumen.mostrar(
                e.getDatoEntero(EventoJuego.CLAVE_MOVIMIENTOS, 0),
                e.getDatoEntero(EventoJuego.CLAVE_EMPUJES, 0),
                e.getDatoEntero(EventoJuego.CLAVE_UNDOS, 0),
                e.getDatoEntero(EventoJuego.CLAVE_PUNTAJE, 0),
                gestorLogros.logrosDesbloqueados().size(),
                gestorLogros.getLogros().size(),
                this::pasarAlSiguienteNivel,
                () -> {
                    juego.reiniciarNivel();
                    panelTablero.requestFocusInWindow();
                });
    }

    private void pasarAlSiguienteNivel() {
        if (!juego.siguienteNivel()) {
            mostrarFinal();
            return;
        }
        panelTablero.requestFocusInWindow();
    }

    /** Se completo el ultimo nivel: muestra la pantalla final con sus dos salidas. */
    private void mostrarFinal() {
        setGlassPane(panelFinal);
        panelFinal.mostrar(this::partidaNueva, this::cerrarVentana);
    }

    /**
     * Partida nueva: vuelve al nivel 1 y borra los logros (GestorLogros.reiniciar,
     * SOK-14). Lo resuelve la ventana porque es la duena de GestorLogros; Juego
     * no conoce a los logros (decision 13 de la bitacora).
     */
    private void partidaNueva() {
        gestorLogros.reiniciar();
        juego.iniciarJuego();
        panelTablero.requestFocusInWindow();
    }

    /** Cierra por el mismo camino que la X de la ventana, para respetar el DefaultCloseOperation. */
    private void cerrarVentana() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
