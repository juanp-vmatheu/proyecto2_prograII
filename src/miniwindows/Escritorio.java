package miniwindows;

import miniwindows.excepciones.UsuarioDuplicadoException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Escritorio extends JFrame {

    private Usuario usuarioActual;
    private GestorUsuarios gestorUsuarios;

    public Escritorio(Usuario usuarioActual, GestorUsuarios gestorUsuarios) {
        super("Mini-Windows - " + usuarioActual.getNombreUsuario());
        this.usuarioActual = usuarioActual;
        this.gestorUsuarios = gestorUsuarios;
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel bienvenida = new JLabel("  Sesion de " + usuarioActual.getNombreCompleto()
                + (usuarioActual.isAdministrador() ? " (administrador)" : ""));
        bienvenida.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(bienvenida, BorderLayout.NORTH);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.add(crearBotonApp("Explorador"));
        barra.add(crearBotonApp("Editor de texto"));
        barra.add(crearBotonApp("Visor de imagenes"));
        barra.add(crearBotonApp("Consola"));
        barra.add(crearBotonApp("Reproductor"));

        if (usuarioActual.isAdministrador()) {
            JButton botonNuevoUsuario = new JButton("Nuevo usuario");
            botonNuevoUsuario.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evento) {
                    abrirDialogoNuevoUsuario();
                }
            });
            barra.add(botonNuevoUsuario);
        }

        JButton botonCerrarSesion = new JButton("Cerrar sesion");
        botonCerrarSesion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cerrarSesion();
            }
        });
        barra.add(botonCerrarSesion);

        add(barra, BorderLayout.CENTER);
    }

    private JButton crearBotonApp(String nombre) {
        JButton boton = new JButton(nombre);
        boton.setEnabled(false);
        boton.setToolTipText("Proximamente");
        return boton;
    }

    private void abrirDialogoNuevoUsuario() {
        JTextField campoNombre = new JTextField();
        JTextField campoUsuario = new JTextField();
        JPasswordField campoPassword = new JPasswordField();
        JCheckBox casillaAdmin = new JCheckBox("Es administrador");

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Nombre completo:"));
        panel.add(campoNombre);
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Contrasenia:"));
        panel.add(campoPassword);
        panel.add(casillaAdmin);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Nuevo usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String nombre = campoNombre.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            gestorUsuarios.crearUsuario(nombre, usuario, password, casillaAdmin.isSelected());
            JOptionPane.showMessageDialog(this, "Usuario '" + usuario + "' creado correctamente.");
        } catch (UsuarioDuplicadoException excepcion) {
            JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        dispose();
        VentanaLogin login = new VentanaLogin();
        login.setVisible(true);
    }
}
