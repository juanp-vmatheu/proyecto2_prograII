package insta;

import insta.ClienteInsta;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PanelCargarImagen extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final JLabel etiquetaArchivo = new JLabel("Ninguna imagen seleccionada");
    private final JTextArea areaDescripcion = new JTextArea(6, 30);
    private final JTextField campoCarpeta = new JTextField(15);
    private String rutaImagenSeleccionada;

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

        add(centro, BorderLayout.CENTER);

        JButton botonPublicar = new JButton("Publicar");
        botonPublicar.addActionListener(e -> publicar());
        EstiloMinecraft.aplicarBoton(botonPublicar);
        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        EstiloMinecraft.aplicarPanel(inferior);
        inferior.add(botonPublicar);
        add(inferior, BorderLayout.SOUTH);
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
        String descripcion = areaDescripcion.getText().trim();
        if (descripcion.length() > 220) {
            descripcion = descripcion.substring(0, 220);
        }
        if (descripcion.isEmpty() && rutaImagenSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Agrega una descripcion o una imagen antes de publicar.");
            return;
        }

        String rutaFinal = rutaImagenSeleccionada;
        if (rutaImagenSeleccionada != null && !campoCarpeta.getText().trim().isEmpty()) {
            rutaFinal = copiarAFolderPersonal(rutaImagenSeleccionada, campoCarpeta.getText().trim());
        }

        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.PUBLICAR, miUsername,
                rutaFinal == null ? "" : rutaFinal, "", descripcion));

        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Publicacion creada.");
            areaDescripcion.setText("");
            campoCarpeta.setText("");
            rutaImagenSeleccionada = null;
            etiquetaArchivo.setText("Ninguna imagen seleccionada");
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    private String copiarAFolderPersonal(String rutaOrigen, String nombreCarpeta) {
        try {
            File origen = new File(rutaOrigen);
            File carpetaDestino = new File(RutasInsta.carpetaFoldersPersonales(miUsername)
                    + File.separator + nombreCarpeta);
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdirs();
            }
            File destino = new File(carpetaDestino, origen.getName());
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destino.getAbsolutePath();
        } catch (Exception e) {
            return rutaOrigen;
        }
    }
}