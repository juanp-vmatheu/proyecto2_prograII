package insta;

import insta.ClienteInsta;
import insta.Usuario;
import insta.ServidorInsta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaInsta extends JFrame {

    private final ClienteInsta cliente;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);
    private final PanelLogin panelLogin;
    private final PanelRegistro panelRegistro;
    private PanelPrincipal panelPrincipal;

    public VentanaInsta(String hostServidor) {
        super("INSTA+");
        this.cliente = new ClienteInsta(hostServidor, ServidorInsta.PUERTO);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        EstiloMinecraft.aplicarVentana(this);
        EstiloMinecraft.aplicarPanel(contenedor);

        panelLogin = new PanelLogin(cliente, this::mostrarRegistro, this::iniciarSesion);
        panelRegistro = new PanelRegistro(cliente, this::mostrarLogin);

        contenedor.add(panelLogin, "LOGIN");
        contenedor.add(panelRegistro, "REGISTRO");

        add(contenedor);
        cardLayout.show(contenedor, "LOGIN");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                if (panelPrincipal != null) {
                    panelPrincipal.detenerNotificaciones();
                }
            }
        });
    }

    private void mostrarRegistro() {
        panelRegistro.limpiar();
        cardLayout.show(contenedor, "REGISTRO");
    }

    private void mostrarLogin() {
        panelLogin.limpiar();
        cardLayout.show(contenedor, "LOGIN");
    }

    private void iniciarSesion(Usuario usuario) {
        panelPrincipal = new PanelPrincipal(cliente, usuario, this::cerrarSesion);
        contenedor.add(panelPrincipal, "APP");
        cardLayout.show(contenedor, "APP");
    }

    private void cerrarSesion() {
        panelPrincipal = null;
        contenedor.removeAll();
        panelLogin.limpiar();
        contenedor.add(panelLogin, "LOGIN");
        contenedor.add(panelRegistro, "REGISTRO");
        cardLayout.show(contenedor, "LOGIN");
        revalidate();
        repaint();
    }
}
