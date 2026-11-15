package sokoban.vista;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

        panelTablero.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                manejarTecla(e);
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        juego.iniciarJuego();
        pack();
        setLocationRelativeTo(null);
        panelTablero.requestFocusInWindow();
    }

    /**
     * Traduce la tecla a una accion del controlador. El modelo nunca ve un
     * KeyEvent.
     */
    private void manejarTecla(KeyEvent e) {
        Direccion direccion = aDireccion(e.getKeyCode());
        if (direccion != null) {
            juego.manejarEntrada(direccion);
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_Z:
                juego.deshacerMovimiento();
                break;
            case KeyEvent.VK_R:
                juego.reiniciarNivel();
                break;
            default:
                break;
        }
    }

    private Direccion aDireccion(int codigo) {
        switch (codigo) {
            case KeyEvent.VK_UP:
                return Direccion.ARRIBA;
            case KeyEvent.VK_DOWN:
                return Direccion.ABAJO;
            case KeyEvent.VK_LEFT:
                return Direccion.IZQUIERDA;
            case KeyEvent.VK_RIGHT:
                return Direccion.DERECHA;
            default:
                return null;
        }
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
            JOptionPane.showMessageDialog(this,
                    "Terminaste todos los niveles disponibles.",
                    "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        panelTablero.requestFocusInWindow();
    }
}
