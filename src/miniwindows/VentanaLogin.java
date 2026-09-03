package miniwindows;

import miniwindows.excepciones.CredencialesInvalidasException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaLogin extends JFrame {

    private GestorUsuarios gestorUsuarios;
    private JTextField campoUsuario;
    private JPasswordField campoPassword;

    public VentanaLogin() {
        super("Mini-Windows - Iniciar sesion");
        gestorUsuarios = new GestorUsuarios();
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 220);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout());

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(8, 8, 8, 8);
        restricciones.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Mini-Windows");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        restricciones.gridx = 0;
        restricciones.gridy = 0;
        restricciones.gridwidth = 2;
        add(titulo, restricciones);

        restricciones.gridwidth = 1;
        restricciones.gridy = 1;
        restricciones.gridx = 0;
        add(new JLabel("Usuario:"), restricciones);

        campoUsuario = new JTextField(15);
        restricciones.gridx = 1;
        add(campoUsuario, restricciones);

        restricciones.gridy = 2;
        restricciones.gridx = 0;
        add(new JLabel("Contrasenia:"), restricciones);

        campoPassword = new JPasswordField(15);
        restricciones.gridx = 1;
        add(campoPassword, restricciones);

        JButton botonEntrar = new JButton("Iniciar sesion");
        botonEntrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                intentarLogin();
            }
        });
        restricciones.gridy = 3;
        restricciones.gridx = 0;
        restricciones.gridwidth = 2;
        add(botonEntrar, restricciones);

        getRootPane().setDefaultButton(botonEntrar);
    }

    private void intentarLogin() {
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());
        try {
            Usuario logueado = gestorUsuarios.validarCredenciales(usuario, password);
            dispose();
            Escritorio escritorio = new Escritorio(logueado, gestorUsuarios);
            escritorio.setVisible(true);
        } catch (CredencialesInvalidasException excepcion) {
            JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Error de acceso", JOptionPane.ERROR_MESSAGE);
            campoPassword.setText("");
        }
    }
}
