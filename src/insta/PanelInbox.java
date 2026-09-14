package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Mensaje;
import insta.Sticker;
import insta.TipoMensaje;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelInbox extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final DefaultListModel<String> modeloConversaciones = new DefaultListModel<>();
    private final JList<String> listaConversaciones = new JList<>(modeloConversaciones);
    private final List<String> usernamesConversaciones = new ArrayList<>();

    private final DefaultListModel<String> modeloMensajes = new DefaultListModel<>();
    private final JList<String> listaMensajes = new JList<>(modeloMensajes);

    private final JTextField campoMensaje = new JTextField(20);
    private String conversacionActual;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public PanelInbox(ClienteInsta cliente, String miUsername) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

        JPanel izquierda = new JPanel(new BorderLayout(5, 5));
        izquierda.setPreferredSize(new Dimension(190, 0));
        EstiloMinecraft.aplicarPanel(izquierda);
        izquierda.add(new JLabel("Conversaciones"), BorderLayout.NORTH);
        listaConversaciones.setBackground(EstiloMinecraft.GRIS_FONDO);
        JScrollPane scrollConversaciones = new JScrollPane(listaConversaciones);
        EstiloMinecraft.aplicarRanura(scrollConversaciones);
        izquierda.add(scrollConversaciones, BorderLayout.CENTER);
        JButton botonNueva = new JButton("Nuevo mensaje");
        botonNueva.addActionListener(e -> iniciarConversacionNueva());
        EstiloMinecraft.aplicarBoton(botonNueva);
        izquierda.add(botonNueva, BorderLayout.SOUTH);
        add(izquierda, BorderLayout.WEST);

        JPanel derecha = new JPanel(new BorderLayout(5, 5));
        EstiloMinecraft.aplicarPanel(derecha);

        JPanel accionesConversacion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(accionesConversacion);
        JButton botonEliminar = new JButton("Eliminar conversacion");
        EstiloMinecraft.aplicarBoton(botonEliminar);
        accionesConversacion.add(botonEliminar);
        derecha.add(accionesConversacion, BorderLayout.NORTH);

        listaMensajes.setBackground(EstiloMinecraft.GRIS_FONDO);
        JScrollPane scrollMensajes = new JScrollPane(listaMensajes);
        EstiloMinecraft.aplicarRanura(scrollMensajes);
        derecha.add(scrollMensajes, BorderLayout.CENTER);

        JPanel envio = new JPanel(new BorderLayout(5, 5));
        EstiloMinecraft.aplicarPanel(envio);
        envio.add(campoMensaje, BorderLayout.CENTER);
        JButton botonEnviar = new JButton("Enviar");
        JButton botonSticker = new JButton("Sticker");
        EstiloMinecraft.aplicarBoton(botonEnviar);
        EstiloMinecraft.aplicarBoton(botonSticker);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        EstiloMinecraft.aplicarPanel(botones);
        botones.add(botonSticker);
        botones.add(botonEnviar);
        envio.add(botones, BorderLayout.EAST);
        derecha.add(envio, BorderLayout.SOUTH);

        add(derecha, BorderLayout.CENTER);

        listaConversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaConversaciones.getSelectedIndex() >= 0
                    && listaConversaciones.getSelectedIndex() < usernamesConversaciones.size()) {
                abrirConversacion(usernamesConversaciones.get(listaConversaciones.getSelectedIndex()));
            }
        });
        botonEnviar.addActionListener(e -> enviarMensaje());
        campoMensaje.addActionListener(e -> enviarMensaje());
        botonSticker.addActionListener(e -> enviarSticker());
        botonEliminar.addActionListener(e -> eliminarConversacion());
    }

    @SuppressWarnings("unchecked")
    public void cargarConversaciones() {
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.LISTAR_CONVERSACIONES, miUsername));
        modeloConversaciones.clear();
        usernamesConversaciones.clear();
        if (respuesta.isExito()) {
            ListaEnlazada<String> conversaciones = (ListaEnlazada<String>) respuesta.getDatos();
            for (String u : conversaciones) {
                modeloConversaciones.addElement(u);
                usernamesConversaciones.add(u);
            }
        }
    }

    public void actualizarEnVivo() {
        cargarConversaciones();
        if (conversacionActual != null) {
            int indice = usernamesConversaciones.indexOf(conversacionActual);
            if (indice >= 0) {
                listaConversaciones.setSelectedIndex(indice);
            }
            abrirConversacion(conversacionActual);
        }
    }

    private void iniciarConversacionNueva() {
        String destino = JOptionPane.showInputDialog(this, "Username del destinatario:");
        if (destino != null && !destino.trim().isEmpty()) {
            abrirConversacion(destino.trim());
        }
    }

    @SuppressWarnings("unchecked")
    private void abrirConversacion(String otroUsuario) {
        conversacionActual = otroUsuario;
        cliente.enviar(cliente.armar(Protocolo.MARCAR_LEIDO, miUsername, otroUsuario));
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.CONVERSACION, miUsername, otroUsuario));
        modeloMensajes.clear();
        if (!respuesta.isExito()) {
            modeloMensajes.addElement("Error: " + respuesta.getMensaje());
            return;
        }
        ListaEnlazada<Mensaje> mensajes = (ListaEnlazada<Mensaje>) respuesta.getDatos();
        if (mensajes.estaVacia()) {
            modeloMensajes.addElement("Todavia no hay mensajes con " + otroUsuario + ".");
        }
        for (Mensaje m : mensajes) {
            String contenido = m.getTipo() == TipoMensaje.STICKER ? "[sticker] " + m.getContenido() : m.getContenido();
            modeloMensajes.addElement(m.getEmisor() + " (" + m.getFechaHora().format(FORMATO) + "): " + contenido);
        }
    }

    private void enviarMensaje() {
        if (conversacionActual == null) {
            JOptionPane.showMessageDialog(this, "Selecciona o inicia una conversacion primero.");
            return;
        }
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ENVIAR_MENSAJE, miUsername, conversacionActual, texto));
        if (respuesta.isExito()) {
            campoMensaje.setText("");
            abrirConversacion(conversacionActual);
            cargarConversaciones();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    @SuppressWarnings("unchecked")
    private void enviarSticker() {
        if (conversacionActual == null) {
            JOptionPane.showMessageDialog(this, "Selecciona o inicia una conversacion primero.");
            return;
        }
        Respuesta disponibles = cliente.enviar(cliente.armar(Protocolo.STICKERS_DISPONIBLES, miUsername));
        if (!disponibles.isExito()) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los stickers.");
            return;
        }
        ListaEnlazada<Sticker> stickers = (ListaEnlazada<Sticker>) disponibles.getDatos();
        String[] nombres = new String[stickers.tamano()];
        for (int i = 0; i < stickers.tamano(); i++) {
            nombres[i] = stickers.obtener(i).getNombre();
        }
        if (nombres.length == 0) {
            JOptionPane.showMessageDialog(this, "No tienes stickers disponibles.");
            return;
        }
        String elegido = (String) JOptionPane.showInputDialog(this, "Elige un sticker:", "Stickers",
                JOptionPane.PLAIN_MESSAGE, null, nombres, nombres[0]);
        if (elegido != null) {
            Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ENVIAR_STICKER, miUsername, conversacionActual, elegido));
            if (respuesta.isExito()) {
                abrirConversacion(conversacionActual);
                cargarConversaciones();
            }
        }
    }

    private void eliminarConversacion() {
        if (conversacionActual == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar toda la conversacion con " + conversacionActual + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            cliente.enviar(cliente.armar(Protocolo.ELIMINAR_CONVERSACION, miUsername, conversacionActual));
            modeloMensajes.clear();
            conversacionActual = null;
            cargarConversaciones();
        }
    }
}