package insta;

import insta.ClienteInsta;
import insta.Usuario;
import insta.ServidorInsta;

import javax.swing.*;
import java.awt.*;

public class VentanaInsta extends JFrame {

    private final ClienteInsta cliente;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);
    private final PanelLogin panelLogin;
    private final PanelRegistro panelRegistro;

    public VentanaInsta(String hostServidor) {
        super("INSTA+");
        this.cliente = new ClienteInsta(hostServidor, ServidorInsta.PUERTO);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        panelLogin = new PanelLogin(cliente, this::mostrarRegistro, this::iniciarSesion);
        panelRegistro = new PanelRegistro(cliente, this::mostrarLogin);

        contenedor.add(panelLogin, "LOGIN");
        contenedor.add(panelRegistro, "REGISTRO");

        add(contenedor);
        cardLayout.show(contenedor, "LOGIN");
    }

    private void mostrarRegistro() {
        cardLayout.show(contenedor, "REGISTRO");
    }

    private void mostrarLogin() {
        cardLayout.show(contenedor, "LOGIN");
    }

    private void iniciarSesion(Usuario usuario) {
        PanelPrincipal panelPrincipal = new PanelPrincipal(cliente, usuario, this::cerrarSesion);
        contenedor.add(panelPrincipal, "APP");
        cardLayout.show(contenedor, "APP");
    }

    private void cerrarSesion() {
        contenedor.removeAll();
        panelLogin.limpiar();
        contenedor.add(panelLogin, "LOGIN");
        contenedor.add(panelRegistro, "REGISTRO");
        cardLayout.show(contenedor, "LOGIN");
        revalidate();
        repaint();
    }
}