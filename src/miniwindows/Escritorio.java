package miniwindows;

import miniwindows.apps.ConsolaGUI;
import miniwindows.apps.EditorTexto;
import miniwindows.apps.EstiloMinecraft;
import miniwindows.apps.ExploradorArchivos;
import miniwindows.apps.Reproductor;
import miniwindows.apps.VisorImagenes;
import miniwindows.excepciones.UsuarioDuplicadoException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Escritorio extends JFrame {

    private static final String CARPETA_ICONOS = "recursos/iconos/";
    private static final String CARPETA_FONDOS = "recursos/fondos/";
    private static final int TAMANIO_ICONO = 38;

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
        setSize(900, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        PanelFondo panelFondo = new PanelFondo(CARPETA_FONDOS + "escritorio.png");
        panelFondo.setLayout(new BorderLayout());
        setContentPane(panelFondo);

        JLabel bienvenida = new JLabel("  Sesion de " + usuarioActual.getNombreCompleto()
                + (usuarioActual.isAdministrador() ? " (administrador)" : ""));
        bienvenida.setFont(new Font("SansSerif", Font.BOLD, 14));
        bienvenida.setOpaque(true);
        bienvenida.setBackground(new Color(20, 20, 20));
        bienvenida.setForeground(Color.WHITE);
        bienvenida.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panelFondo.add(bienvenida, BorderLayout.NORTH);

        panelFondo.add(armarIconosEscritorio(), BorderLayout.WEST);
        panelFondo.add(armarEsquinaCerrarSesion(), BorderLayout.SOUTH);
    }

    private JPanel armarIconosEscritorio() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 12, 12, 12));

        panel.add(crearBotonExplorador());
        panel.add(Box.createVerticalStrut(8));
        panel.add(crearBotonEditor());
        panel.add(Box.createVerticalStrut(8));
        panel.add(crearBotonVisor());
        panel.add(Box.createVerticalStrut(8));
        panel.add(crearBotonConsola());
        panel.add(Box.createVerticalStrut(8));
        panel.add(crearBotonReproductor());
        if (usuarioActual.isAdministrador()) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(crearBotonNuevoUsuario());
        }

        return panel;
    }

    private JPanel armarEsquinaCerrarSesion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        panel.setOpaque(false);
        panel.add(crearBotonCerrarSesion());
        return panel;
    }

    private ImageIcon cargarIcono(String nombreArchivo) {
        File archivo = new File(CARPETA_ICONOS + nombreArchivo);
        if (!archivo.exists()) {
            return null;
        }
        ImageIcon original = new ImageIcon(archivo.getPath());
        Image escalada = original.getImage().getScaledInstance(TAMANIO_ICONO, TAMANIO_ICONO, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    private JButton crearIconoEscritorio(String texto, String nombreIcono) {
        JButton boton = new JButton(texto);
        ImageIcon icono = cargarIcono(nombreIcono);
        if (icono != null) {
            boton.setIcon(icono);
        }
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setForeground(Color.WHITE);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 11f));
        boton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        return boton;
    }

    private JButton crearBotonExplorador() {
        JButton boton = crearIconoEscritorio("Explorador", "explorador.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                ExploradorArchivos explorador = new ExploradorArchivos(usuarioActual);
                explorador.setVisible(true);
            }
        });
        return boton;
    }

    private JButton crearBotonEditor() {
        JButton boton = crearIconoEscritorio("Editor de texto", "editor.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                EditorTexto editor = new EditorTexto(usuarioActual);
                editor.setVisible(true);
            }
        });
        return boton;
    }

    private JButton crearBotonVisor() {
        JButton boton = crearIconoEscritorio("Visor de imagenes", "visor.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                VisorImagenes visor = new VisorImagenes(usuarioActual);
                visor.setVisible(true);
            }
        });
        return boton;
    }

    private JButton crearBotonReproductor() {
        JButton boton = crearIconoEscritorio("Reproductor", "reproductor.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                Reproductor reproductor = new Reproductor(usuarioActual);
                reproductor.setVisible(true);
            }
        });
        return boton;
    }

    private JButton crearBotonConsola() {
        JButton boton = crearIconoEscritorio("Consola", "consola.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                ConsolaGUI consola = new ConsolaGUI(usuarioActual.getNombreUsuario());
                consola.mostrarVentana();
            }
        });
        return boton;
    }

    private JButton crearBotonNuevoUsuario() {
        JButton boton = crearIconoEscritorio("Nuevo usuario", "nuevo_usuario.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                abrirDialogoNuevoUsuario();
            }
        });
        return boton;
    }

    private JButton crearBotonCerrarSesion() {
        JButton boton = crearIconoEscritorio("Cerrar sesion", "apagar.png");
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cerrarSesion();
            }
        });
        return boton;
    }

    private void abrirDialogoNuevoUsuario() {
        JTextField campoNombre = new JTextField();
        JTextField campoUsuario = new JTextField();
        final JPasswordField campoPassword = new JPasswordField();
        final JPasswordField campoConfirmarPassword = new JPasswordField();
        final char caracterOcultoPassword = campoPassword.getEchoChar();
        JCheckBox casillaAdmin = new JCheckBox("Es administrador");
        casillaAdmin.setOpaque(false);
        JCheckBox casillaMostrarPassword = new JCheckBox("Mostrar contrasenia");
        casillaMostrarPassword.setOpaque(false);
        casillaMostrarPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                char caracter = ((JCheckBox) evento.getSource()).isSelected() ? (char) 0 : caracterOcultoPassword;
                campoPassword.setEchoChar(caracter);
                campoConfirmarPassword.setEchoChar(caracter);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        EstiloMinecraft.aplicarPanel(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Nombre completo:"));
        panel.add(campoNombre);
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Contrasenia:"));
        panel.add(campoPassword);
        panel.add(new JLabel("Confirmar contrasenia:"));
        panel.add(campoConfirmarPassword);
        panel.add(casillaMostrarPassword);
        panel.add(casillaAdmin);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Nuevo usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String nombre = campoNombre.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());
        String confirmarPassword = new String(campoConfirmarPassword.getPassword());

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmarPassword)) {
            JOptionPane.showMessageDialog(this, "Las contrasenias no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
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
