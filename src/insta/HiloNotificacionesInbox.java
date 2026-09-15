package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Protocolo;
import insta.Respuesta;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;

public class HiloNotificacionesInbox extends Thread {

    private final ClienteInsta cliente;
    private final String username;
    private final NotificacionListener listener;
    private volatile boolean activo = true;

    public interface NotificacionListener {
        void cambioEnInbox(ListaEnlazada<String> remitentesNoLeidos, boolean llegoMensajeNuevo);
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
    @SuppressWarnings("unchecked")
    public void run() {
        int cantidadNoLeidosAnterior = -1;
        LocalDateTime ultimoMensajeAnterior = null;
        while (activo) {
            try {
                Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ESTADO_INBOX, username));
                if (respuesta.isExito()) {
                    Object[] datos = (Object[]) respuesta.getDatos();
                    ListaEnlazada<String> remitentesNoLeidos = (ListaEnlazada<String>) datos[0];
                    LocalDateTime ultimoMensaje = (LocalDateTime) datos[1];
                    boolean llegoMensajeNuevo = ultimoMensaje != null && !ultimoMensaje.equals(ultimoMensajeAnterior);
                    if (llegoMensajeNuevo || remitentesNoLeidos.tamano() != cantidadNoLeidosAnterior) {
                        cantidadNoLeidosAnterior = remitentesNoLeidos.tamano();
                        ultimoMensajeAnterior = ultimoMensaje;
                        SwingUtilities.invokeLater(() -> listener.cambioEnInbox(remitentesNoLeidos, llegoMensajeNuevo));
                    }
                }
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
