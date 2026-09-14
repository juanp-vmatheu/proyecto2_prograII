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

public class PanelPerfil extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;

    private final JLabel etiquetaNombre = new JLabel();
    private final JLabel etiquetaUsername = new JLabel();
    private final JLabel etiquetaDatos = new JLabel();
    private final JLabel etiquetaContadores = new JLabel();
    private final JLabel etiquetaEstado = new JLabel();
    private final JButton botonSeguir = new JButton();
    private final JButton botonVerPublicaciones = new JButton("Ver sus publicaciones");

    private String usernameMostrado;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PanelPerfil(ClienteInsta cliente, String miUsername) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

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
        add(datos, BorderLayout.NORTH);

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

    public void mostrarPerfil(String username) {
        this.usernameMostrado = username;
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.PERFIL, username));
        if (!respuesta.isExito()) {
            etiquetaNombre.setText("Usuario no encontrado");
            etiquetaUsername.setText("");
            etiquetaDatos.setText("");
            etiquetaContadores.setText("");
            etiquetaEstado.setText("");
            botonSeguir.setVisible(false);
            botonVerPublicaciones.setVisible(false);
            return;
        }
        Object[] datos = (Object[]) respuesta.getDatos();
        Usuario u = (Usuario) datos[0];
        int followers = (int) datos[1];
        int following = (int) datos[2];
        int publicaciones = (int) datos[3];

        etiquetaNombre.setText(u.getNombreCompleto());
        etiquetaUsername.setText("@" + u.getUsername());
        etiquetaDatos.setText("Edad: " + u.getEdad() + "   Genero: " + u.getGenero()
                + "   Registrado: " + u.getFechaRegistro().format(FORMATO_FECHA));
        etiquetaContadores.setText(followers + (followers == 1 ? " seguidor    " : " seguidores    ")
                + following + " siguiendo    "
                + publicaciones + (publicaciones == 1 ? " publicacion" : " publicaciones"));
        etiquetaEstado.setText("Estado: " + (u.isActiva() ? "Activa" : "Inactiva"));

        boolean esMiPerfil = username.equalsIgnoreCase(miUsername);
        botonVerPublicaciones.setVisible(true);
        botonSeguir.setVisible(!esMiPerfil);
        if (!esMiPerfil) {
            actualizarBotonSeguir();
        }
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
        if (yoLoSigo()) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Dejar de seguir a " + usernameMostrado + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            cliente.enviar(cliente.armar(Protocolo.DEJAR_SEGUIR, miUsername, usernameMostrado));
        } else {
            cliente.enviar(cliente.armar(Protocolo.SEGUIR, miUsername, usernameMostrado));
        }
        mostrarPerfil(usernameMostrado);
    }

    private void verPublicaciones() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Publicaciones de " + usernameMostrado, true);
        dialogo.setLayout(new BorderLayout());
        dialogo.getContentPane().setBackground(EstiloMinecraft.GRIS_FONDO);

        PanelFeed feed = new PanelFeed(cliente, PanelFeed.Modo.DE_USUARIO);
        feed.cargar(usernameMostrado);
        dialogo.add(feed, BorderLayout.CENTER);

        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.addActionListener(e -> dialogo.dispose());
        EstiloMinecraft.aplicarBoton(botonCerrar);
        JPanel piePagina = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        EstiloMinecraft.aplicarPanel(piePagina);
        piePagina.add(botonCerrar);
        dialogo.add(piePagina, BorderLayout.SOUTH);

        dialogo.setSize(450, 520);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
}