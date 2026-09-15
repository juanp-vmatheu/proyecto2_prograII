package insta;

import insta.ClienteInsta;
import insta.Usuario;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class PanelEditarPerfil extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final JTextField campoNombre = new JTextField(20);
    private final JLabel etiquetaFoto = new JLabel("Sin cambios");
    private final JLabel etiquetaVistaFoto = new JLabel();
    private final JLabel etiquetaEstado = new JLabel();
    private String rutaFotoActual;
    private String rutaFotoNueva;
    private static final int TAMANIO_VISTA_FOTO = 80;

    public PanelEditarPerfil(ClienteInsta cliente, String miUsername) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        EstiloMinecraft.aplicarPanel(formulario);

        JPanel filaVistaFoto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaVistaFoto);
        etiquetaVistaFoto.setPreferredSize(new Dimension(TAMANIO_VISTA_FOTO, TAMANIO_VISTA_FOTO));
        etiquetaVistaFoto.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaVistaFoto.setOpaque(true);
        etiquetaVistaFoto.setBackground(EstiloMinecraft.GRIS_RANURA);
        EstiloMinecraft.aplicarRanura(etiquetaVistaFoto);
        filaVistaFoto.add(etiquetaVistaFoto);
        formulario.add(filaVistaFoto);
        formulario.add(Box.createVerticalStrut(10));

        JPanel filaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaNombre);
        filaNombre.add(new JLabel("Nombre completo:"));
        filaNombre.add(campoNombre);
        formulario.add(filaNombre);

        JPanel filaFoto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaFoto);
        JButton botonFoto = new JButton("Cambiar foto de perfil");
        botonFoto.addActionListener(e -> seleccionarFoto());
        EstiloMinecraft.aplicarBoton(botonFoto);
        filaFoto.add(botonFoto);
        filaFoto.add(etiquetaFoto);
        formulario.add(filaFoto);

        JPanel filaGuardar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaGuardar);
        JButton botonGuardar = new JButton("Guardar cambios");
        botonGuardar.addActionListener(e -> guardar());
        EstiloMinecraft.aplicarBoton(botonGuardar);
        filaGuardar.add(botonGuardar);
        formulario.add(filaGuardar);

        formulario.add(Box.createVerticalStrut(25));
        JPanel filaEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaEstado);
        filaEstado.add(etiquetaEstado);
        formulario.add(filaEstado);

        JPanel filaBotonEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(filaBotonEstado);
        JButton botonEstado = new JButton("Activar / Desactivar cuenta");
        botonEstado.addActionListener(e -> alternarEstado());
        EstiloMinecraft.aplicarBoton(botonEstado);
        filaBotonEstado.add(botonEstado);
        formulario.add(filaBotonEstado);

        add(formulario, BorderLayout.NORTH);
    }

    public void cargar() {
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.PERFIL, miUsername));
        if (respuesta.isExito()) {
            Usuario u = (Usuario) ((Object[]) respuesta.getDatos())[0];
            campoNombre.setText(u.getNombreCompleto());
            etiquetaEstado.setText("Estado actual: " + (u.isActiva() ? "Activa" : "Inactiva"));
            rutaFotoActual = u.getFotoPerfil();
        }
        rutaFotoNueva = null;
        etiquetaFoto.setText("Sin cambios");
        etiquetaVistaFoto.setIcon(CargadorIconos.cargarEscalado(rutaFotoActual, TAMANIO_VISTA_FOTO));
    }

    private void seleccionarFoto() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imagenes (png, jpg)", "png", "jpg", "jpeg"));
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            rutaFotoNueva = selector.getSelectedFile().getAbsolutePath();
            etiquetaFoto.setText(selector.getSelectedFile().getName());
            etiquetaVistaFoto.setIcon(CargadorIconos.cargarEscalado(rutaFotoNueva, TAMANIO_VISTA_FOTO));
        }
    }

    private void guardar() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre completo no puede quedar vacio.");
            return;
        }
        if (nombre.contains(Protocolo.SEPARADOR)) {
            JOptionPane.showMessageDialog(this, "El nombre no puede contener el caracter " + Protocolo.SEPARADOR);
            return;
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ACTUALIZAR_PERFIL, miUsername,
                nombre, rutaFotoNueva == null ? "" : rutaFotoNueva));
        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Perfil actualizado.");
            cargar();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
    }

    private void alternarEstado() {
        Respuesta perfilActual = cliente.enviar(cliente.armar(Protocolo.PERFIL, miUsername));
        boolean activaActual = true;
        if (perfilActual.isExito()) {
            Usuario u = (Usuario) ((Object[]) perfilActual.getDatos())[0];
            activaActual = u.isActiva();
        }
        if (activaActual) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres desactivar tu cuenta?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.ACTIVAR_DESACTIVAR, miUsername));
        if (!respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
            return;
        }
        boolean activaAhora = (boolean) respuesta.getDatos();
        etiquetaEstado.setText("Estado actual: " + (activaAhora ? "Activa" : "Inactiva"));
        JOptionPane.showMessageDialog(this, activaAhora ? "Cuenta reactivada." : "Cuenta desactivada.");
    }
}
