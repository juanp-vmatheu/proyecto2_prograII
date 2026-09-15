package insta;

import insta.ClienteInsta;
import insta.HiloNotificacionesInbox;
import insta.Usuario;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;

public class PanelPrincipal extends JPanel {

    private final ClienteInsta cliente;
    private final Usuario usuario;
    private final Runnable alCerrarSesion;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenido = new JPanel(cardLayout);

    private final PanelPerfil panelPerfil;
    private final PanelCargarImagen panelCargarImagen;
    private final PanelFeed panelTimeline;
    private final PanelFeed panelInteracciones;
    private final PanelBuscarProfile panelBuscarProfile;
    private final PanelBuscarHashtag panelBuscarHashtag;
    private final PanelInbox panelInbox;
    private final PanelEditarPerfil panelEditarPerfil;
    private final PanelFeed panelPublicacionesUsuario;

    private final JButton botonInbox = new JButton("Inbox");
    private HiloNotificacionesInbox hiloNotificaciones;
    private String tarjetaActual = "PERFIL";
    private String tarjetaAntesDePublicaciones = "PERFIL";

    public PanelPrincipal(ClienteInsta cliente, Usuario usuario, Runnable alCerrarSesion) {
        this.cliente = cliente;
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout());
        EstiloMinecraft.aplicarPanel(this);
        EstiloMinecraft.aplicarPanel(contenido);

        panelPerfil = new PanelPerfil(cliente, usuario.getUsername(), this::mostrarPublicacionesDe);
        panelCargarImagen = new PanelCargarImagen(cliente, usuario.getUsername());
        panelTimeline = new PanelFeed(cliente, PanelFeed.Modo.TIMELINE);
        panelInteracciones = new PanelFeed(cliente, PanelFeed.Modo.INTERACCIONES);
        panelBuscarProfile = new PanelBuscarProfile(cliente, usuario.getUsername(), this::mostrarPublicacionesDe);
        panelBuscarHashtag = new PanelBuscarHashtag(cliente);
        panelInbox = new PanelInbox(cliente, usuario.getUsername());
        panelEditarPerfil = new PanelEditarPerfil(cliente, usuario.getUsername());
        panelPublicacionesUsuario = new PanelFeed(cliente, PanelFeed.Modo.DE_USUARIO);

        contenido.add(panelPerfil, "PERFIL");
        contenido.add(panelCargarImagen, "CARGAR");
        contenido.add(panelTimeline, "TIMELINE");
        contenido.add(panelInteracciones, "INTERACCIONES");
        contenido.add(panelBuscarProfile, "BUSCAR_PROFILE");
        contenido.add(panelBuscarHashtag, "BUSCAR_HASHTAG");
        contenido.add(panelInbox, "INBOX");
        contenido.add(panelEditarPerfil, "EDITAR");
        contenido.add(construirPanelVerPublicaciones(), "PUBLICACIONES_DE_USUARIO");

        add(construirMenu(), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        mostrar("PERFIL");
        iniciarNotificaciones();
    }

    private JPanel construirMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        menu.setPreferredSize(new Dimension(180, 0));
        EstiloMinecraft.aplicarPanel(menu);

        JLabel titulo = new JLabel("INSTA+");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(titulo);
        JLabel usernameLabel = new JLabel("@" + usuario.getUsername());
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(usernameLabel);
        menu.add(Box.createVerticalStrut(15));

        menu.add(botonMenu("Perfil", "PERFIL"));
        menu.add(botonMenu("Cargar imagenes", "CARGAR"));
        menu.add(botonMenu("Comentarios", "TIMELINE"));
        menu.add(botonMenu("Interacciones", "INTERACCIONES"));
        menu.add(botonMenu("Buscar profile", "BUSCAR_PROFILE"));
        menu.add(botonMenu("Buscar hashtag", "BUSCAR_HASHTAG"));

        botonInbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonInbox.setMaximumSize(new Dimension(Integer.MAX_VALUE, botonInbox.getPreferredSize().height));
        botonInbox.addActionListener(e -> mostrar("INBOX"));
        EstiloMinecraft.aplicarBoton(botonInbox);
        menu.add(botonInbox);

        menu.add(botonMenu("Editar perfil", "EDITAR"));

        menu.add(Box.createVerticalGlue());
        JButton botonCerrar = new JButton("Cerrar sesion");
        botonCerrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonCerrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, botonCerrar.getPreferredSize().height));
        botonCerrar.addActionListener(e -> cerrarSesion());
        EstiloMinecraft.aplicarBoton(botonCerrar);
        menu.add(botonCerrar);

        return menu;
    }

    private JPanel construirPanelVerPublicaciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        EstiloMinecraft.aplicarPanel(panel);
        panel.add(panelPublicacionesUsuario, BorderLayout.CENTER);

        JButton botonVolver = new JButton("Volver");
        EstiloMinecraft.aplicarBoton(botonVolver);
        botonVolver.addActionListener(e -> mostrar(tarjetaAntesDePublicaciones));
        JPanel piePagina = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(piePagina);
        piePagina.add(botonVolver);
        panel.add(piePagina, BorderLayout.SOUTH);

        return panel;
    }

    private void mostrarPublicacionesDe(String username) {
        if (!"PUBLICACIONES_DE_USUARIO".equals(tarjetaActual)) {
            tarjetaAntesDePublicaciones = tarjetaActual;
        }
        panelPublicacionesUsuario.cargar(username);
        tarjetaActual = "PUBLICACIONES_DE_USUARIO";
        cardLayout.show(contenido, "PUBLICACIONES_DE_USUARIO");
    }

    private JButton botonMenu(String texto, String tarjeta) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
        boton.addActionListener(e -> mostrar(tarjeta));
        EstiloMinecraft.aplicarBoton(boton);
        return boton;
    }

    private void mostrar(String tarjeta) {
        tarjetaActual = tarjeta;
        cardLayout.show(contenido, tarjeta);
        switch (tarjeta) {
            case "PERFIL":
                panelPerfil.mostrarPerfil(usuario.getUsername());
                break;
            case "CARGAR":
                panelCargarImagen.limpiar();
                break;
            case "TIMELINE":
                panelTimeline.cargar(usuario.getUsername());
                break;
            case "INTERACCIONES":
                panelInteracciones.cargar(usuario.getUsername());
                break;
            case "BUSCAR_PROFILE":
                panelBuscarProfile.refrescar();
                break;
            case "BUSCAR_HASHTAG":
                panelBuscarHashtag.refrescar();
                break;
            case "INBOX":
                panelInbox.refrescar();
                break;
            case "EDITAR":
                panelEditarPerfil.cargar();
                break;
            default:
                break;
        }
    }

    private void iniciarNotificaciones() {
        hiloNotificaciones = new HiloNotificacionesInbox(cliente, usuario.getUsername(), (remitentesNoLeidos, llegoMensajeNuevo) -> {
            int cantidad = remitentesNoLeidos.tamano();
            botonInbox.setText(cantidad > 0 ? "Inbox (" + cantidad + ")" : "Inbox");
            if ("INBOX".equals(tarjetaActual)) {
                panelInbox.actualizarEnVivo(remitentesNoLeidos, llegoMensajeNuevo);
            }
        });
        hiloNotificaciones.start();
    }

    public void detenerNotificaciones() {
        if (hiloNotificaciones != null) {
            hiloNotificaciones.detener();
        }
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Cerrar sesion?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            detenerNotificaciones();
            alCerrarSesion.run();
        }
    }
}
