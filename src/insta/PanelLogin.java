package insta;

import insta.ClienteInsta;
import insta.Usuario;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PanelLogin extends JPanel {

    private final ClienteInsta cliente;
    private final JTextField campoUsername = new JTextField(15);
    private final JPasswordField campoPassword = new JPasswordField(15);
    private final char caracterOcultoPassword = campoPassword.getEchoChar();
    private final JLabel etiquetaError = new JLabel(" ");

    public PanelLogin(ClienteInsta cliente, Runnable irARegistro, Consumer<Usuario> alIniciarSesion) {
        this.cliente = cliente;
        setLayout(new GridBagLayout());
        EstiloMinecraft.aplicarPanel(this);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        JLabel titulo = new JLabel("INSTA+");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        add(titulo, c);

        c.gridwidth = 1;
        c.gridy++;
        add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        add(campoUsername, c);

        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        add(campoPassword, c);

        JCheckBox casillaMostrarPassword = new JCheckBox("Mostrar contraseña");
        casillaMostrarPassword.setOpaque(false);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        add(casillaMostrarPassword, c);
        c.gridwidth = 1;

        JButton botonLogin = new JButton("Iniciar sesion");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        add(botonLogin, c);

        JButton botonIrRegistro = new JButton("Crear cuenta nueva");
        c.gridy++;
        add(botonIrRegistro, c);

        c.gridy++;
        etiquetaError.setForeground(Color.RED);
        add(etiquetaError, c);

        EstiloMinecraft.aplicarBoton(botonLogin);
        EstiloMinecraft.aplicarBoton(botonIrRegistro);

        casillaMostrarPassword.addActionListener(e ->
                campoPassword.setEchoChar(casillaMostrarPassword.isSelected() ? (char) 0 : caracterOcultoPassword));
        botonLogin.addActionListener(e -> intentarLogin(alIniciarSesion));
        botonIrRegistro.addActionListener(e -> irARegistro.run());
        campoPassword.addActionListener(e -> intentarLogin(alIniciarSesion));
    }

    private void intentarLogin(Consumer<Usuario> alIniciarSesion) {
        String username = campoUsername.getText().trim();
        String password = new String(campoPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            etiquetaError.setText("Completa usuario y contraseña.");
            return;
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.LOGIN, username, password));
        if (respuesta.isExito()) {
            etiquetaError.setText(" ");
            alIniciarSesion.accept((Usuario) respuesta.getDatos());
        } else {
            etiquetaError.setText(respuesta.getMensaje());
        }
    }

    public void limpiar() {
        campoUsername.setText("");
        campoPassword.setText("");
        etiquetaError.setText(" ");
    }
}