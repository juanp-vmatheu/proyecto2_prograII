package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Publicacion;
import insta.Protocolo;
import insta.Respuesta;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class PanelFeed extends JPanel {

    public enum Modo { TIMELINE, INTERACCIONES, HASHTAG, DE_USUARIO }

    private final ClienteInsta cliente;
    private final Modo modo;
    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modeloLista);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelFeed(ClienteInsta cliente, Modo modo) {
        this.cliente = cliente;
        this.modo = modo;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        lista.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(new JScrollPane(lista), BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    public void cargar(String parametro) {
        Respuesta respuesta;
        switch (modo) {
            case TIMELINE:
                respuesta = cliente.enviar(cliente.armar(Protocolo.TIMELINE, parametro));
                break;
            case INTERACCIONES:
                respuesta = cliente.enviar(cliente.armar(Protocolo.INTERACCIONES, parametro));
                break;
            case HASHTAG:
                respuesta = cliente.enviar(cliente.armar(Protocolo.BUSCAR_HASHTAG, parametro));
                break;
            case DE_USUARIO:
                respuesta = cliente.enviar(cliente.armar(Protocolo.PUBLICACIONES_DE, parametro));
                break;
            default:
                respuesta = Respuesta.error("Modo no soportado");
        }
        modeloLista.clear();
        if (!respuesta.isExito()) {
            modeloLista.addElement("Error: " + respuesta.getMensaje());
            return;
        }
        ListaEnlazada<Publicacion> publicaciones = (ListaEnlazada<Publicacion>) respuesta.getDatos();
        if (publicaciones.estaVacia()) {
            modeloLista.addElement("Sin publicaciones por ahora.");
            return;
        }
        for (Publicacion p : publicaciones) {
            StringBuilder texto = new StringBuilder("<html><b>").append(p.getAutor()).append("</b> escribio:<br>\"")
                    .append(p.getContenido() == null ? "" : p.getContenido()).append("\"");
            if (p.getImagenRuta() != null) {
                texto.append("<br><i>[imagen adjunta]</i>");
            }
            if (p.getStickerRuta() != null) {
                texto.append("<br><i>[sticker]</i>");
            }
            texto.append("<br><font color='gray'>").append(p.getFecha().format(FORMATO)).append("</font></html>");
            modeloLista.addElement(texto.toString());
        }
    }
}