package sokoban.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sokoban.controlador.Juego;
import sokoban.eventos.EventoJuego;
import sokoban.eventos.GestorEventos;
import sokoban.eventos.Observador;
import sokoban.eventos.TipoEvento;
import sokoban.logros.GestorLogros;
import sokoban.modelo.Direccion;
import sokoban.powerups.GestorPowerUps;

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

    private final GestorEventos gestorEventos = new GestorEventos();
    private final GestorPowerUps gestorPowerUps = new GestorPowerUps();
    private final GestorLogros gestorLogros = new GestorLogros(gestorEventos);

    private final Juego juego = new Juego(gestorEventos, gestorPowerUps);

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
        pack();
        setLocationRelativeTo(null);
        panelTablero.requestFocusInWindow();
    }
}
