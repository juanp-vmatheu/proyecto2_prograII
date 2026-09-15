package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Usuario;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class PanelPerfil extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;
    private final Consumer<String> alVerPublicaciones;
    private Runnable alCambiarSeguimiento;

    private final JLabel etiquetaFoto = new JLabel();
    private final JLabel etiquetaNombre = new JLabel();
    private final JLabel etiquetaUsername = new JLabel();
    private final JLabel etiquetaDatos = new JLabel();
    private final JLabel etiquetaContadores = new JLabel();
    private final JLabel etiquetaEstado = new JLabel();
    private final JButton botonSeguir = new JButton();
    private final JButton botonVerPublicaciones = new JButton("Ver sus publicaciones");

    private String usernameMostrado;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TAMANIO_FOTO_PERFIL = 90;

    public PanelPerfil(ClienteInsta cliente, String miUsername, Consumer<String> alVerPublicaciones) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        this.alVerPublicaciones = alVerPublicaciones;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

        JPanel encabezado = new JPanel(new BorderLayout(15, 0));
        EstiloMinecraft.aplicarPanel(encabezado);

        etiquetaFoto.setPreferredSize(new Dimension(TAMANIO_FOTO_PERFIL, TAMANIO_FOTO_PERFIL));
        etiquetaFoto.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaFoto.setBackground(EstiloMinecraft.GRIS_RANURA);
        etiquetaFoto.setOpaque(true);
        EstiloMinecraft.aplicarRanura(etiquetaFoto);
        encabezado.add(etiquetaFoto, BorderLayout.WEST);

        JPanel datos = new JPanel();
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        EstiloMinecraft.aplicarPanel(datos);
        etiquetaNombre.setFont(new Font("SansSerif", Font.BOLD, 20));
        etiquetaUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));
        datos.add(etiquetaNombre);
        datos.add(etiquetaUsername);
        datos.add(Box.createVerticalStrut(10));
        datos.add(etiquetaDatos);
        datos.add(etiquetaContadores);
        datos.add(etiquetaEstado);
        encabezado.add(datos, BorderLayout.CENTER);

        add(encabezado, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(acciones);
        acciones.add(botonSeguir);
        acciones.add(botonVerPublicaciones);
        add(acciones, BorderLayout.SOUTH);

        EstiloMinecraft.aplicarBoton(botonSeguir);
        EstiloMinecraft.aplicarBoton(botonVerPublicaciones);

        botonSeguir.addActionListener(e -> alternarSeguir());
        botonVerPublicaciones.addActionListener(e -> verPublicaciones());

        botonSeguir.setVisible(false);
        botonVerPublicaciones.setVisible(false);
    }

    public void setAlCambiarSeguimiento(Runnable alCambiarSeguimiento) {
        this.alCambiarSeguimiento = alCambiarSeguimiento;
    }

    public String getUsernameMostrado() {
        return usernameMostrado;
    }

    public void refrescar() {
        if (usernameMostrado != null) {
            mostrarPerfil(usernameMostrado);
        }
    }

    public void mostrarPerfil(String username) {
        this.usernameMostrado = username;
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.PERFIL, username));
        if (!respuesta.isExito()) {
            mostrarNoEncontrado(respuesta.getMensaje());
            return;
        }
        Object[] datos = (Object[]) respuesta.getDatos();
        Usuario u = (Usuario) datos[0];
        int followers = (int) datos[1];
        int following = (int) datos[2];
        int publicaciones = (int) datos[3];

        boolean esMiPerfil = u.getUsername().equalsIgnoreCase(miUsername);
        if (!esMiPerfil && !u.isActiva()) {
            mostrarNoEncontrado("Usuario no encontrado");
            return;
        }
        this.usernameMostrado = u.getUsername();

        etiquetaFoto.setIcon(CargadorIconos.cargarEscalado(u.getFotoPerfil(), TAMANIO_FOTO_PERFIL));
        etiquetaNombre.setText(u.getNombreCompleto());
        etiquetaUsername.setText("@" + u.getUsername());
        etiquetaDatos.setText("Edad: " + u.getEdad() + "   Genero: " + u.getGenero()
                + "   Registrado: " + u.getFechaRegistro().format(FORMATO_FECHA));
        etiquetaContadores.setText(followers + (followers == 1 ? " seguidor    " : " seguidores    ")
                + following + " siguiendo    "
                + publicaciones + (publicaciones == 1 ? " publicacion" : " publicaciones"));
        etiquetaEstado.setText("Estado: " + (u.isActiva() ? "Activa" : "Inactiva"));

        botonVerPublicaciones.setText(esMiPerfil ? "Ver mis publicaciones" : "Ver sus publicaciones");
        botonVerPublicaciones.setVisible(true);
        botonSeguir.setVisible(!esMiPerfil);
        if (!esMiPerfil) {
            actualizarBotonSeguir();
        }
    }

    private void mostrarNoEncontrado(String mensaje) {
        etiquetaFoto.setIcon(null);
        etiquetaNombre.setText(mensaje);
        etiquetaUsername.setText("");
        etiquetaDatos.setText("");
        etiquetaContadores.setText("");
        etiquetaEstado.setText("");
        botonSeguir.setVisible(false);
        botonVerPublicaciones.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    private boolean yoLoSigo() {
        Respuesta siguiendo = cliente.enviar(cliente.armar(Protocolo.FOLLOWING, miUsername));
        if (!siguiendo.isExito()) {
            return false;
        }
        ListaEnlazada<String> lista = (ListaEnlazada<String>) siguiendo.getDatos();
        return lista.contiene(usernameMostrado);
    }

    private void actualizarBotonSeguir() {
        botonSeguir.setText(yoLoSigo() ? "Dejar de seguir" : "Seguir");
    }

    private void alternarSeguir() {
        Respuesta respuesta;
        if (yoLoSigo()) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Dejar de seguir a " + usernameMostrado + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            respuesta = cliente.enviar(cliente.armar(Protocolo.DEJAR_SEGUIR, miUsername, usernameMostrado));
        } else {
            respuesta = cliente.enviar(cliente.armar(Protocolo.SEGUIR, miUsername, usernameMostrado));
        }
        if (!respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Error: " + respuesta.getMensaje());
        }
        mostrarPerfil(usernameMostrado);
        if (alCambiarSeguimiento != null) {
            alCambiarSeguimiento.run();
        }
    }

    private void verPublicaciones() {
        alVerPublicaciones.accept(usernameMostrado);
    }
}
