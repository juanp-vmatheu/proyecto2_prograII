package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.archivos.ArchivoBinario;
import miniwindows.archivos.DocumentoTexto;
import miniwindows.excepciones.ArchivoCorruptoException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class EditorTexto extends JFrame {

    private static final String[] FUENTES = {"SansSerif", "Serif", "Monospaced", "Arial", "Times New Roman", "Courier New"};
    private static final Integer[] TAMANIOS = {10, 12, 14, 16, 18, 20, 24, 28, 32};

    private File raizNavegable;
    private File archivoActual;
    private JTextArea areaTexto;
    private JComboBox<String> comboFuente;
    private JComboBox<Integer> comboTamanio;
    private Color colorActual = Color.BLACK;

    public EditorTexto(Usuario usuarioActual) {
        super("Editor de texto");
        raizNavegable = usuarioActual.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        areaTexto = new JTextArea();
        areaTexto.setFont(new Font(FUENTES[0], Font.PLAIN, 14));
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        add(armarBarraHerramientas(), BorderLayout.NORTH);
    }

    private JPanel armarBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

        JButton botonNuevo = new JButton("Nuevo");
        botonNuevo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                nuevoDocumento();
            }
        });

        JButton botonAbrir = new JButton("Abrir");
        botonAbrir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                abrirDocumento();
            }
        });

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                guardarDocumento();
            }
        });

        comboFuente = new JComboBox<String>(FUENTES);
        comboFuente.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                aplicarFormato();
            }
        });

        comboTamanio = new JComboBox<Integer>(TAMANIOS);
        comboTamanio.setSelectedItem(14);
        comboTamanio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                aplicarFormato();
            }
        });

        JButton botonColor = new JButton("Color");
        botonColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                elegirColor();
            }
        });

        barra.add(botonNuevo);
        barra.add(botonAbrir);
        barra.add(botonGuardar);
        barra.add(comboFuente);
        barra.add(comboTamanio);
        barra.add(botonColor);
        return barra;
    }

    private void aplicarFormato() {
        String nombreFuente = (String) comboFuente.getSelectedItem();
        int tamanio = (Integer) comboTamanio.getSelectedItem();
        areaTexto.setFont(new Font(nombreFuente, Font.PLAIN, tamanio));
        areaTexto.setForeground(colorActual);
    }

    private void elegirColor() {
        Color elegido = JColorChooser.showDialog(this, "Color del texto", colorActual);
        if (elegido != null) {
            colorActual = elegido;
            aplicarFormato();
        }
    }

    private void nuevoDocumento() {
        archivoActual = null;
        areaTexto.setText("");
        comboFuente.setSelectedIndex(0);
        comboTamanio.setSelectedItem(14);
        colorActual = Color.BLACK;
        aplicarFormato();
        setTitle("Editor de texto");
    }

    private void abrirDocumento() {
        JFileChooser selector = new JFileChooser(raizNavegable);
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        try {
            Object leido = ArchivoBinario.leerObjeto(archivo.getPath());
            DocumentoTexto documento = (DocumentoTexto) leido;
            areaTexto.setText(documento.getTexto());
            comboFuente.setSelectedItem(documento.getNombreFuente());
            comboTamanio.setSelectedItem(documento.getTamanioFuente());
            colorActual = new Color(documento.getColorRGB());
            aplicarFormato();
            archivoActual = archivo;
            setTitle("Editor de texto - " + archivo.getName());
        } catch (ArchivoCorruptoException excepcion) {
            JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarDocumento() {
        if (archivoActual == null) {
            JFileChooser selector = new JFileChooser(raizNavegable);
            int resultado = selector.showSaveDialog(this);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File elegido = selector.getSelectedFile();
            if (!elegido.getName().toLowerCase().endsWith(".txt")) {
                elegido = new File(elegido.getPath() + ".txt");
            }
            archivoActual = elegido;
        }
        String nombreFuente = (String) comboFuente.getSelectedItem();
        int tamanio = (Integer) comboTamanio.getSelectedItem();
        DocumentoTexto documento = new DocumentoTexto(areaTexto.getText(), nombreFuente, tamanio, colorActual.getRGB());
        try {
            ArchivoBinario.guardarObjeto(archivoActual.getPath(), documento);
            setTitle("Editor de texto - " + archivoActual.getName());
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
