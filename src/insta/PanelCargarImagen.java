package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Sticker;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PanelCargarImagen extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final JLabel etiquetaArchivo = new JLabel("Ninguna imagen seleccionada");
    private final JLabel etiquetaSticker = new JLabel("Ningun sticker seleccionado");
    private final JButton botonSticker = new JButton("Agregar sticker (opcional)");
    private final JTextArea areaDescripcion = new JTextArea(6, 30);
    private final JTextField campoCarpeta = new JTextField(15);
    private String rutaImagenSeleccionada;
    private String rutaStickerSeleccionado;
    private static final int TAMANIO_STICKER_SELECTOR = 48;
    private static final int MAX_CARACTERES_DESCRIPCION = 220;

    public PanelCargarImagen(ClienteInsta cliente, String miUsername) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(superior);
        JButton botonSeleccionar = new JButton("Seleccionar imagen");
        botonSeleccionar.addActionListener(e -> seleccionarImagen());
        EstiloMinecraft.aplicarBoton(botonSeleccionar);
        superior.add(botonSeleccionar);
        superior.add(etiquetaArchivo);
        add(superior, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        EstiloMinecraft.aplicarPanel(centro);
        centro.add(new JLabel("Descripcion (usa @usuario para mencionar y #palabra para hashtag, maximo 220 caracteres):"));
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(areaDescripcion);
        EstiloMinecraft.aplicarRanura(scrollDescripcion);
        centro.add(scrollDescripcion);
        centro.add(Box.createVerticalStrut(10));

        JPanel filaCarpeta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaCarpeta);
        filaCarpeta.add(new JLabel("Carpeta personal (opcional):"));
        filaCarpeta.add(campoCarpeta);
        centro.add(filaCarpeta);

        JPanel filaSticker = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaSticker);
        botonSticker.addActionListener(e -> mostrarSelectorStickers());
        EstiloMinecraft.aplicarBoton(botonSticker);
        filaSticker.add(botonSticker);
        filaSticker.add(etiquetaSticker);
        centro.add(filaSticker);

        add(centro, BorderLayout.CENTER);

        JButton botonPublicar = new JButton("Publicar");
        botonPublicar.addActionListener(e -> publicar());
        EstiloMinecraft.aplicarBoton(botonPublicar);
        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        EstiloMinecraft.aplicarPanel(inferior);
        inferior.add(botonPublicar);
        add(inferior, BorderLayout.SOUTH);
    }

    public void limpiar() {
        areaDescripcion.setText("");
        campoCarpeta.setText("");
        rutaImagenSeleccionada = null;
        etiquetaArchivo.setText("Ninguna imagen seleccionada");
        rutaStickerSeleccionado = null;
        etiquetaSticker.setText("Ningun sticker seleccionado");
    }

    private void seleccionarImagen() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            rutaImagenSeleccionada = selector.getSelectedFile().getAbsolutePath();
            etiquetaArchivo.setText(selector.getSelectedFile().getName());
        }
    }

    private void publicar() {
        String descripcion = areaDescripcion.getText().trim().replace('\r', ' ').replace('\n', ' ');
        if (descripcion.isEmpty() && rutaImagenSeleccionada == null && rutaStickerSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Agrega una descripcion, una imagen o un sticker antes de publicar.");
            return;
        }
        if (descripcion.length() > MAX_CARACTERES_DESCRIPCION) {
            JOptionPane.showMessageDialog(this, "La descripcion tiene " + descripcion.length()
                    + " caracteres, el maximo es " + MAX_CARACTERES_DESCRIPCION + ".");
            return;
        }
        String carpetaPersonal = campoCarpeta.getText().trim();
        if (!carpetaPersonal.isEmpty() && !carpetaPersonal.matches("[\\p{L}\\p{N} _-]+")) {
            JOptionPane.showMessageDialog(this, "El nombre de la carpeta solo puede tener letras, numeros, espacios, _ o -.");
            return;
        }

        String rutaFinal = "";
        if (rutaImagenSeleccionada != null) {
            try {
                rutaFinal = guardarImagen(rutaImagenSeleccionada, carpetaPersonal);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la imagen: " + e.getMessage());
                return;
            }
        }

        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.PUBLICAR, miUsername,
                rutaFinal, rutaStickerSeleccionado == null ? "" : rutaStickerSeleccionado, descripcion));

        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Publicacion creada.");
            limpiar();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    @SuppressWarnings("unchecked")
    private void mostrarSelectorStickers() {
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
            JButton boton = new JButton(CargadorIconos.cargarEscalado(sticker.getRuta(), TAMANIO_STICKER_SELECTOR));
            boton.setToolTipText(sticker.getNombre());
            boton.setContentAreaFilled(false);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.addActionListener(e -> {
                popup.setVisible(false);
                rutaStickerSeleccionado = sticker.getRuta();
                etiquetaSticker.setText(sticker.getNombre());
            });
            panelStickers.add(boton);
        }

        popup.add(panelStickers);
        Dimension tamanio = panelStickers.getPreferredSize();
        popup.show(botonSticker, 0, botonSticker.getHeight());
        popup.setPreferredSize(tamanio);
    }

    private String guardarImagen(String rutaOrigen, String carpetaPersonal) throws IOException {
        File origen = new File(rutaOrigen);
        String nombreArchivo = System.currentTimeMillis() + "_" + origen.getName().replace(Protocolo.SEPARADOR, "_");

        File carpetaImagenes = new File(RutasInsta.carpetaImagenes(miUsername));
        if (!carpetaImagenes.exists()) {
            carpetaImagenes.mkdirs();
        }
        File destino = new File(carpetaImagenes, nombreArchivo);
        Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

        if (!carpetaPersonal.isEmpty()) {
            File carpetaDestino = new File(RutasInsta.carpetaFoldersPersonales(miUsername), carpetaPersonal);
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdirs();
            }
            Files.copy(origen.toPath(), new File(carpetaDestino, nombreArchivo).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return destino.getPath();
    }
}
