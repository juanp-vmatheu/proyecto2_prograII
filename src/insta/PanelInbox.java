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
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelInbox extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final DefaultListModel<String> modeloConversaciones = new DefaultListModel<>();
    private final JList<String> listaConversaciones = new JList<>(modeloConversaciones);
    private final List<String> usernamesConversaciones = new ArrayList<>();

    private final DefaultListModel<Object> modeloMensajes = new DefaultListModel<>();
    private final JList<Object> listaMensajes = new JList<>(modeloMensajes);

    private final JTextField campoMensaje = new JTextField(20);
    private final JButton botonSticker = new JButton("Sticker");
    private String conversacionActual;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final int TAMANIO_STICKER_MENSAJE = 64;
    private static final int TAMANIO_STICKER_SELECTOR = 48;

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
        listaMensajes.setCellRenderer(new RenderizadorMensajes());
        JScrollPane scrollMensajes = new JScrollPane(listaMensajes);
        EstiloMinecraft.aplicarRanura(scrollMensajes);
        derecha.add(scrollMensajes, BorderLayout.CENTER);

        JPanel envio = new JPanel(new BorderLayout(5, 5));
        EstiloMinecraft.aplicarPanel(envio);
        envio.add(campoMensaje, BorderLayout.CENTER);
        JButton botonEnviar = new JButton("Enviar");
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
        botonSticker.addActionListener(e -> mostrarSelectorStickers());
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
            modeloMensajes.addElement(m);
        }
        if (!modeloMensajes.isEmpty()) {
            listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
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
    private void mostrarSelectorStickers() {
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

        JPopupMenu popup = new JPopupMenu();
        JPanel panelStickers = new JPanel(new GridLayout(0, 4, 6, 6));
        EstiloMinecraft.aplicarPanel(panelStickers);
        panelStickers.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Sticker sticker : stickers) {
            JButton boton = new JButton(cargarIconoEscalado(sticker.getRuta(), TAMANIO_STICKER_SELECTOR));
            boton.setToolTipText(sticker.getNombre());
            boton.setContentAreaFilled(false);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.addActionListener(e -> {
                popup.setVisible(false);
                enviarStickerElegido(sticker);
            });
            panelStickers.add(boton);
        }

        JButton botonAgregar = new JButton("+");
        botonAgregar.setToolTipText("Agregar sticker nuevo");
        botonAgregar.setFont(botonAgregar.getFont().deriveFont(Font.BOLD, 22f));
        botonAgregar.setPreferredSize(new Dimension(TAMANIO_STICKER_SELECTOR, TAMANIO_STICKER_SELECTOR));
        EstiloMinecraft.aplicarBoton(botonAgregar);
        botonAgregar.addActionListener(e -> {
            popup.setVisible(false);
            importarStickerNuevo();
        });
        panelStickers.add(botonAgregar);

        popup.add(panelStickers);
        Dimension tamanio = panelStickers.getPreferredSize();
        popup.show(botonSticker, 0, -(tamanio.height + 30));
    }

    private void importarStickerNuevo() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        String nombre = JOptionPane.showInputDialog(this, "Nombre para el sticker:",
                archivo.getName().replaceFirst("\\.[^.]+$", ""));
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.IMPORTAR_STICKER,
                miUsername, nombre.trim(), archivo.getAbsolutePath()));
        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Sticker agregado.");
            mostrarSelectorStickers();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    private void enviarStickerElegido(Sticker sticker) {
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ENVIAR_STICKER, miUsername, conversacionActual, sticker.getRuta()));
        if (respuesta.isExito()) {
            abrirConversacion(conversacionActual);
            cargarConversaciones();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    private static ImageIcon cargarIconoEscalado(String ruta, int tamano) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return null;
        }
        ImageIcon original = new ImageIcon(archivo.getPath());
        Image escalada = original.getImage().getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
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

    private class RenderizadorMensajes extends JLabel implements ListCellRenderer<Object> {

        RenderizadorMensajes() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                        boolean seleccionado, boolean tieneFoco) {
            setBackground(seleccionado ? EstiloMinecraft.GRIS_RANURA : EstiloMinecraft.GRIS_FONDO);
            setIcon(null);
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalTextPosition(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.TRAILING);
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            if (valor instanceof Mensaje) {
                Mensaje m = (Mensaje) valor;
                String encabezado = m.getEmisor() + " (" + m.getFechaHora().format(FORMATO) + "):";
                if (m.getTipo() == TipoMensaje.STICKER) {
                    setIcon(cargarIconoEscalado(m.getContenido(), TAMANIO_STICKER_MENSAJE));
                    setText(encabezado);
                    setVerticalTextPosition(SwingConstants.BOTTOM);
                    setHorizontalTextPosition(SwingConstants.CENTER);
                } else {
                    setText(encabezado + " " + m.getContenido());
                }
            } else {
                setText(String.valueOf(valor));
            }
            return this;
        }
    }
}