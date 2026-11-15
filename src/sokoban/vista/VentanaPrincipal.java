package sokoban.vista;

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
    private final PanelHUD panelHUD = new PanelHUD(juego, this::partidaNueva);
    private final PanelNotificaciones panelNotificaciones = new PanelNotificaciones();
    private final ReproductorSonido reproductorSonido = new ReproductorSonido();

    /** Suma de los puntajes de los niveles completados en esta partida. */
    private int puntajeTotal;

    public VentanaPrincipal() {
        super("Sokoban - PDS");

        gestorEventos.agregarObservador(gestorLogros);
        gestorEventos.agregarObservador(panelTablero);
        gestorEventos.agregarObservador(panelHUD);
        gestorEventos.agregarObservador(panelNotificaciones);
        gestorEventos.agregarObservador(reproductorSonido);
        gestorEventos.agregarObservador(this);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setBackground(new Color(38, 38, 46));
        superior.add(panelHUD, BorderLayout.NORTH);
        superior.add(panelNotificaciones, BorderLayout.SOUTH);

        // El tablero se centra: si el HUD es mas ancho que el nivel, el
        // tablero no se estira, queda centrado sobre el fondo.
        JPanel centrador = new JPanel(new GridBagLayout());
        centrador.setBackground(new Color(24, 24, 30));
        centrador.add(panelTablero);

        setLayout(new BorderLayout());
        add(superior, BorderLayout.NORTH);
        add(centrador, BorderLayout.CENTER);

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
        puntajeTotal += e.getDatoEntero(EventoJuego.CLAVE_PUNTAJE, 0);
        StringBuilder texto = new StringBuilder();
        texto.append("Nivel ").append(e.getNivel()).append(" completado\n\n");
        texto.append("Movimientos: ").append(e.getDatoEntero(EventoJuego.CLAVE_MOVIMIENTOS, 0)).append('\n');
        texto.append("Empujes: ").append(e.getDatoEntero(EventoJuego.CLAVE_EMPUJES, 0)).append('\n');
        texto.append("Usos de deshacer: ").append(e.getDatoEntero(EventoJuego.CLAVE_UNDOS, 0)).append('\n');
        texto.append("Puntaje: ").append(e.getDatoEntero(EventoJuego.CLAVE_PUNTAJE, 0)).append("\n\n");
        texto.append("Logros desbloqueados: ").append(gestorLogros.logrosDesbloqueados().size())
                .append(" de ").append(gestorLogros.getLogros().size());

        JOptionPane.showMessageDialog(this, texto.toString(),
                "Resumen del nivel", JOptionPane.INFORMATION_MESSAGE);

        if (!juego.siguienteNivel()) {
            mostrarFinDelJuego();
            return;
        }
        reacomodar();
    }

    /**
     * Fin del juego: total acumulado de la partida y la opcion de empezar de
     * nuevo o cerrar, en vez de quedar congelado sobre el ultimo tablero.
     */
    private void mostrarFinDelJuego() {
        String texto = "Terminaste los " + juego.getNivelActual() + " niveles.\n\n"
                + "Puntaje total: " + puntajeTotal + "\n"
                + "Logros: " + gestorLogros.logrosDesbloqueados().size()
                + " de " + gestorLogros.getLogros().size();
        Object[] opciones = {"Partida nueva", "Cerrar"};
        int eleccion = JOptionPane.showOptionDialog(this, texto, "Fin del juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opciones, opciones[0]);
        if (eleccion == 0) {
            partidaNueva();
        } else {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Arranca de cero: borra los logros (GestorLogros.reiniciar, SOK-14) y el
     * puntaje acumulado, y vuelve al nivel 1. Lo resuelve la ventana porque es
     * la duena de GestorLogros; Juego no conoce a los logros (decision 13).
     */
    private void partidaNueva() {
        gestorLogros.reiniciar();
        puntajeTotal = 0;
        juego.iniciarJuego();
        reacomodar();
    }

    /** Cada nivel tiene otro tamano: la ventana se ajusta y el tablero recupera el foco. */
    private void reacomodar() {
        pack();
        setLocationRelativeTo(null);
        panelTablero.requestFocusInWindow();
    }
}
