package miniwindows.red;

import miniwindows.GestorUsuarios;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSOP extends JFrame {

    private static final int PUERTO = 5000;

    private GestorUsuarios gestorUsuarios;
    private JTextArea areaLog;
    private ServerSocket serverSocket;
    private volatile boolean corriendo;

    public ServidorSOP() {
        super("Servidor de sesiones - Mini-Windows");
        gestorUsuarios = new GestorUsuarios();
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evento) {
                detenerServidor();
            }
        });
    }

    public void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            corriendo = true;
            registrarLog("Servidor iniciado en el puerto " + PUERTO + ".");
        } catch (IOException excepcion) {
            registrarLog("No se pudo iniciar el servidor: " + excepcion.getMessage());
            return;
        }

        Thread hiloAceptador = new Thread(new Runnable() {
            public void run() {
                escucharConexiones();
            }
        });
        hiloAceptador.start();
    }

    private void escucharConexiones() {
        while (corriendo) {
            try {
                Socket cliente = serverSocket.accept();
                registrarLog("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
                HiloCliente hiloCliente = new HiloCliente(cliente, gestorUsuarios, this);
                hiloCliente.start();
            } catch (IOException excepcion) {
                if (corriendo) {
                    registrarLog("Error aceptando conexion: " + excepcion.getMessage());
                }
            }
        }
    }

    private void detenerServidor() {
        corriendo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException excepcion) {
            System.out.println("Error al cerrar el servidor: " + excepcion.getMessage());
        }
    }

    public void registrarLog(final String mensaje) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                areaLog.append(mensaje + "\n");
            }
        });
    }
}
