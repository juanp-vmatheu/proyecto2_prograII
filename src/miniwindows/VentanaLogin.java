package miniwindows;

import miniwindows.excepciones.CredencialesInvalidasException;
import miniwindows.red.ClienteSOP;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class VentanaLogin extends JFrame {

    private static final String CARPETA_FONDOS = "recursos/fondos/";

    private GestorUsuarios gestorUsuarios;
    private JTextField campoUsuario;
    private JPasswordField campoPassword;
    private char caracterOcultoPassword;

    public VentanaLogin() {
        super("Mini-Windows - Iniciar sesion");
        gestorUsuarios = new GestorUsuarios();
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        PanelFondo panelFondo = new PanelFondo(CARPETA_FONDOS + "escritorio.png");
        panelFondo.setLayout(new GridBagLayout());
        setContentPane(panelFondo);

        panelFondo.add(armarTarjeta());
    }

    private JPanel armarTarjeta() {
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(new Color(20, 20, 20));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(8, 8, 8, 8);
        restricciones.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Mini-Windows");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        restricciones.gridx = 0;
        restricciones.gridy = 0;
        restricciones.gridwidth = 2;
        tarjeta.add(titulo, restricciones);

        JLabel etiquetaUsuario = new JLabel("Usuario:");
        etiquetaUsuario.setForeground(Color.WHITE);
        restricciones.gridwidth = 1;
        restricciones.gridy = 1;
        restricciones.gridx = 0;
        tarjeta.add(etiquetaUsuario, restricciones);

        campoUsuario = new JTextField(18);
        restricciones.gridx = 1;
        tarjeta.add(campoUsuario, restricciones);

        JLabel etiquetaPassword = new JLabel("Contrasenia:");
        etiquetaPassword.setForeground(Color.WHITE);
        restricciones.gridy = 2;
        restricciones.gridx = 0;
        tarjeta.add(etiquetaPassword, restricciones);

        campoPassword = new JPasswordField(18);
        caracterOcultoPassword = campoPassword.getEchoChar();
        restricciones.gridx = 1;
        tarjeta.add(campoPassword, restricciones);

        JCheckBox casillaMostrarPassword = new JCheckBox("Mostrar contrasenia");
        casillaMostrarPassword.setForeground(Color.WHITE);
        casillaMostrarPassword.setOpaque(false);
        casillaMostrarPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                if (((JCheckBox) evento.getSource()).isSelected()) {
                    campoPassword.setEchoChar((char) 0);
                } else {
                    campoPassword.setEchoChar(caracterOcultoPassword);
                }
            }
        });
        restricciones.gridy = 3;
        restricciones.gridx = 0;
        restricciones.gridwidth = 2;
        tarjeta.add(casillaMostrarPassword, restricciones);

        JButton botonEntrar = new JButton("Iniciar sesion");
        botonEntrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                intentarLogin();
            }
        });
        restricciones.gridy = 4;
        restricciones.gridx = 0;
        restricciones.gridwidth = 2;
        tarjeta.add(botonEntrar, restricciones);

        getRootPane().setDefaultButton(botonEntrar);

        return tarjeta;
    }

    private void intentarLogin() {
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());

        try {
            String[] respuesta = ClienteSOP.login(usuario, password);
            if ("OK".equals(respuesta[0])) {
                Usuario logueado = new Usuario(respuesta[1], usuario, password, Boolean.parseBoolean(respuesta[2]));
                abrirEscritorio(logueado);
            } else {
                String mensaje = respuesta.length > 1 ? respuesta[1] : "Usuario o contrasenia incorrectos.";
                JOptionPane.showMessageDialog(this, mensaje, "Error de acceso", JOptionPane.ERROR_MESSAGE);
                campoPassword.setText("");
            }
            return;
        } catch (IOException excepcionSocket) {
            // servidor no disponible: cae a modo local
        }

        try {
            Usuario logueado = gestorUsuarios.validarCredenciales(usuario, password);
            abrirEscritorio(logueado);
        } catch (CredencialesInvalidasException excepcion) {
            JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Error de acceso", JOptionPane.ERROR_MESSAGE);
            campoPassword.setText("");
        }
    }

    private void abrirEscritorio(Usuario logueado) {
        dispose();
        Escritorio escritorio = new Escritorio(logueado, gestorUsuarios);
        escritorio.setVisible(true);
    }
}
