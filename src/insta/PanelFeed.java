package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Publicacion;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class PanelFeed extends JPanel {

    public enum Modo { TIMELINE, INTERACCIONES, HASHTAG, DE_USUARIO }

    private final ClienteInsta cliente;
    private final Modo modo;
    private final DefaultListModel<Object> modeloLista = new DefaultListModel<>();
    private final JList<Object> lista = new JList<>(modeloLista);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TAMANIO_IMAGEN_FEED = 96;
    private static final int ANCHO_TEXTO = 420;

    public PanelFeed(ClienteInsta cliente, Modo modo) {
        this.cliente = cliente;
        this.modo = modo;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);
        lista.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lista.setBackground(EstiloMinecraft.GRIS_FONDO);
        lista.setCellRenderer(new RenderizadorPublicaciones());
        JScrollPane scroll = new JScrollPane(lista);
        EstiloMinecraft.aplicarRanura(scroll);
        add(scroll, BorderLayout.CENTER);
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
        } else {
            ListaEnlazada<Publicacion> publicaciones = (ListaEnlazada<Publicacion>) respuesta.getDatos();
            if (publicaciones.estaVacia()) {
                modeloLista.addElement("Sin publicaciones por ahora.");
            }
            for (Publicacion p : publicaciones) {
                modeloLista.addElement(p);
            }
        }
        lista.ensureIndexIsVisible(0);
    }

    static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private class RenderizadorPublicaciones extends JLabel implements ListCellRenderer<Object> {

        RenderizadorPublicaciones() {
            setOpaque(true);
            setVerticalAlignment(SwingConstants.TOP);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                        boolean seleccionado, boolean tieneFoco) {
            setBackground(seleccionado ? EstiloMinecraft.GRIS_RANURA : EstiloMinecraft.GRIS_FONDO);
            setIcon(null);
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalTextPosition(SwingConstants.TOP);
            setHorizontalTextPosition(SwingConstants.TRAILING);
            setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            if (valor instanceof Publicacion) {
                Publicacion p = (Publicacion) valor;
                StringBuilder texto = new StringBuilder("<html><body style='width: ").append(ANCHO_TEXTO).append("px'><b>")
                        .append(escaparHtml(p.getAutor())).append("</b> escribio:<br>\"")
                        .append(escaparHtml(p.getContenido())).append("\"");
                if (p.getStickerRuta() != null) {
                    texto.append("<br><i>[sticker]</i>");
                }
                texto.append("<br><font color='gray'>").append(p.getFecha().format(FORMATO)).append("</font></body></html>");
                setText(texto.toString());
                if (p.getImagenRuta() != null) {
                    setIcon(CargadorIconos.cargarEscalado(p.getImagenRuta(), TAMANIO_IMAGEN_FEED));
                } else if (p.getStickerRuta() != null) {
                    setIcon(CargadorIconos.cargarEscalado(p.getStickerRuta(), TAMANIO_IMAGEN_FEED));
                }
            } else {
                setText(String.valueOf(valor));
            }
            return this;
        }
    }
}
