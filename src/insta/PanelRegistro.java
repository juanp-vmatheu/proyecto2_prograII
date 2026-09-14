package insta;

import insta.ClienteInsta;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;

public class PanelRegistro extends JPanel {

    private final ClienteInsta cliente;
    private final JTextField campoNombre = new JTextField(15);
    private final JComboBox<String> comboGenero = new JComboBox<>(new String[]{"F", "M"});
    private final JTextField campoUsername = new JTextField(15);
    private final JPasswordField campoPassword = new JPasswordField(15);
    private final JPasswordField campoConfirmarPassword = new JPasswordField(15);
    private final char caracterOcultoPassword = campoPassword.getEchoChar();
    private final JSpinner campoEdad = new JSpinner(new SpinnerNumberModel(18, 13, 120, 1));
    private final JLabel etiquetaFoto = new JLabel("Sin foto");
    private final JLabel etiquetaMensaje = new JLabel(" ");
    private String rutaFoto;

    public PanelRegistro(ClienteInsta cliente, Runnable irALogin) {
        this.cliente = cliente;
        setLayout(new GridBagLayout());
        EstiloMinecraft.aplicarPanel(this);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;

        JLabel titulo = new JLabel("Crear cuenta");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(titulo, c);
        c.gridwidth = 1;

        agregarFila(c, "Nombre completo:", campoNombre);
        agregarFila(c, "Genero:", comboGenero);
        agregarFila(c, "Username:", campoUsername);
        agregarFila(c, "Contraseña:", campoPassword);
        agregarFila(c, "Confirmar contraseña:", campoConfirmarPassword);

        JCheckBox casillaMostrarPassword = new JCheckBox("Mostrar contraseña");
        casillaMostrarPassword.setOpaque(false);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        add(casillaMostrarPassword, c);
        c.gridwidth = 1;

        agregarFila(c, "Edad:", campoEdad);

        c.gridy++;
        c.gridx = 0;
        JButton botonFoto = new JButton("Foto de perfil (opcional)");
        botonFoto.addActionListener(e -> seleccionarFoto());
        add(botonFoto, c);
        c.gridx = 1;
        add(etiquetaFoto, c);

        JButton botonCrear = new JButton("Crear cuenta");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        add(botonCrear, c);

        JButton botonVolver = new JButton("Ya tengo cuenta");
        c.gridy++;
        add(botonVolver, c);

        c.gridy++;
        etiquetaMensaje.setForeground(Color.RED);
        add(etiquetaMensaje, c);

        EstiloMinecraft.aplicarBoton(botonFoto);
        EstiloMinecraft.aplicarBoton(botonCrear);
        EstiloMinecraft.aplicarBoton(botonVolver);

        casillaMostrarPassword.addActionListener(e -> {
            char caracter = casillaMostrarPassword.isSelected() ? (char) 0 : caracterOcultoPassword;
            campoPassword.setEchoChar(caracter);
            campoConfirmarPassword.setEchoChar(caracter);
        });
        botonCrear.addActionListener(e -> crearCuenta(irALogin));
        botonVolver.addActionListener(e -> irALogin.run());
    }

    private void agregarFila(GridBagConstraints c, String etiqueta, Component campo) {
        c.gridy++;
        c.gridx = 0;
        add(new JLabel(etiqueta), c);
        c.gridx = 1;
        add(campo, c);
    }

    private void seleccionarFoto() {
        JFileChooser selector = new JFileChooser();
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            rutaFoto = selector.getSelectedFile().getAbsolutePath();
            etiquetaFoto.setText(selector.getSelectedFile().getName());
        }
    }

    private void crearCuenta(Runnable irALogin) {
        String nombre = campoNombre.getText().trim();
        String username = campoUsername.getText().trim();
        String password = new String(campoPassword.getPassword());
        String confirmarPassword = new String(campoConfirmarPassword.getPassword());
        String genero = (String) comboGenero.getSelectedItem();
        int edad = (int) campoEdad.getValue();

        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            etiquetaMensaje.setText("Completa todos los campos obligatorios.");
            return;
        }

        if (!password.equals(confirmarPassword)) {
            etiquetaMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.REGISTRO, nombre, genero, username, password,
                String.valueOf(edad), rutaFoto == null ? "" : rutaFoto));

        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ahora inicia sesion.");
            limpiar();
            irALogin.run();
        } else {
            etiquetaMensaje.setText(respuesta.getMensaje());
        }
    }

    private void limpiar() {
        campoNombre.setText("");
        campoUsername.setText("");
        campoPassword.setText("");
        campoConfirmarPassword.setText("");
        campoEdad.setValue(18);
        etiquetaFoto.setText("Sin foto");
        etiquetaMensaje.setText(" ");
        rutaFoto = null;
    }
}