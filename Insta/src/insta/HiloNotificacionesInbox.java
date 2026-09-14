package insta;

import insta.ClienteInsta;
import insta.Protocolo;
import insta.Respuesta;

import javax.swing.SwingUtilities;

public class HiloNotificacionesInbox extends Thread {

    private final ClienteInsta cliente;
    private final String username;
    private final NotificacionListener listener;
    private volatile boolean activo = true;

    public interface NotificacionListener {
        void nuevoMensaje();
    }

    public HiloNotificacionesInbox(ClienteInsta cliente, String username, NotificacionListener listener) {
        this.cliente = cliente;
        this.username = username;
        this.listener = listener;
        setDaemon(true);
    }

    public void detener() {
        activo = false;
        interrupt();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.HAY_MENSAJES_NUEVOS, username));
                if (respuesta.isExito() && Boolean.TRUE.equals(respuesta.getDatos())) {
                    SwingUtilities.invokeLater(listener::nuevoMensaje);
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}