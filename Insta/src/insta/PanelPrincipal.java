package insta;

import insta.ClienteInsta;
import insta.HiloNotificacionesInbox;
import insta.Usuario;

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

    private final JButton botonInbox = new JButton("Inbox");
    private HiloNotificacionesInbox hiloNotificaciones;

    public PanelPrincipal(ClienteInsta cliente, Usuario usuario, Runnable alCerrarSesion) {
        this.cliente = cliente;
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;
        setLayout(new BorderLayout());

        panelPerfil = new PanelPerfil(cliente, usuario.getUsername());
        panelCargarImagen = new PanelCargarImagen(cliente, usuario.getUsername());
        panelTimeline = new PanelFeed(cliente, PanelFeed.Modo.TIMELINE);
        panelInteracciones = new PanelFeed(cliente, PanelFeed.Modo.INTERACCIONES);
        panelBuscarProfile = new PanelBuscarProfile(cliente, usuario.getUsername());
        panelBuscarHashtag = new PanelBuscarHashtag(cliente);
        panelInbox = new PanelInbox(cliente, usuario.getUsername());
        panelEditarPerfil = new PanelEditarPerfil(cliente, usuario.getUsername());

        contenido.add(panelPerfil, "PERFIL");
        contenido.add(panelCargarImagen, "CARGAR");
        contenido.add(panelTimeline, "TIMELINE");
        contenido.add(panelInteracciones, "INTERACCIONES");
        contenido.add(panelBuscarProfile, "BUSCAR_PROFILE");
        contenido.add(panelBuscarHashtag, "BUSCAR_HASHTAG");
        contenido.add(panelInbox, "INBOX");
        contenido.add(panelEditarPerfil, "EDITAR");

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
        menu.add(botonInbox);

        menu.add(botonMenu("Editar perfil", "EDITAR"));

        menu.add(Box.createVerticalGlue());
        JButton botonCerrar = new JButton("Cerrar sesion");
        botonCerrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonCerrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, botonCerrar.getPreferredSize().height));
        botonCerrar.addActionListener(e -> cerrarSesion());
        menu.add(botonCerrar);

        return menu;
    }

    private JButton botonMenu(String texto, String tarjeta) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
        boton.addActionListener(e -> mostrar(tarjeta));
        return boton;
    }

    private void mostrar(String tarjeta) {
        cardLayout.show(contenido, tarjeta);
        switch (tarjeta) {
            case "PERFIL":
                panelPerfil.mostrarPerfil(usuario.getUsername());
                break;
            case "TIMELINE":
                panelTimeline.cargar(usuario.getUsername());
                break;
            case "INTERACCIONES":
                panelInteracciones.cargar(usuario.getUsername());
                break;
            case "INBOX":
                panelInbox.cargarConversaciones();
                botonInbox.setText("Inbox");
                break;
            case "EDITAR":
                panelEditarPerfil.cargar();
                break;
            default:
                break;
        }
    }

    private void iniciarNotificaciones() {
        hiloNotificaciones = new HiloNotificacionesInbox(cliente, usuario.getUsername(),
                () -> botonInbox.setText("Inbox (nuevo)"));
        hiloNotificaciones.start();
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Cerrar sesion?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (hiloNotificaciones != null) {
                hiloNotificaciones.detener();
            }
            alCerrarSesion.run();
        }
    }
}