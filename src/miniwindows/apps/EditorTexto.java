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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class EditorTexto extends JFrame {

    private static final String[] FUENTES = {"SansSerif", "Serif", "Monospaced", "Arial", "Times New Roman", "Courier New"};
    private static final Integer[] TAMANIOS = {10, 12, 14, 16, 18, 20, 24, 28, 32};

    private File carpetaInicial;
    private File raizPermitida;
    private File archivoActual;
    private JTextArea areaTexto;
    private JComboBox<String> comboFuente;
    private JComboBox<Integer> comboTamanio;
    private Color colorActual = Color.BLACK;
    private boolean cambiosSinGuardar;
    private boolean cargandoDocumento;

    public EditorTexto(Usuario usuarioActual) {
        super("Editor de texto");
        File carpetaUsuario = SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        carpetaInicial = new File(carpetaUsuario, "Mis Documentos");
        raizPermitida = usuarioActual.isAdministrador() ? SistemaArchivos.obtenerRaiz() : carpetaUsuario;
        armarVentana();
        actualizarTitulo();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        EstiloMinecraft.aplicarVentana(this);

        areaTexto = new JTextArea();
        areaTexto.setFont(new Font(FUENTES[0], Font.PLAIN, 14));
        areaTexto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evento) {
                marcarCambios();
            }

            public void removeUpdate(DocumentEvent evento) {
                marcarCambios();
            }

            public void changedUpdate(DocumentEvent evento) {
            }
        });
        JScrollPane scrollTexto = new JScrollPane(areaTexto);
        EstiloMinecraft.aplicarRanura(scrollTexto);
        add(scrollTexto, BorderLayout.CENTER);

        add(armarBarraHerramientas(), BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evento) {
                if (confirmarCierre()) {
                    dispose();
                }
            }
        });
    }

    private JPanel armarBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        EstiloMinecraft.aplicarPanel(barra);

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
                cambiarFormato();
            }
        });

        comboTamanio = new JComboBox<Integer>(TAMANIOS);
        comboTamanio.setSelectedItem(14);
        comboTamanio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cambiarFormato();
            }
        });

        JButton botonColor = new JButton("Color");
        botonColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                elegirColor();
            }
        });

        JButton[] botonesBarra = {botonNuevo, botonAbrir, botonGuardar, botonColor};
        for (JButton boton : botonesBarra) {
            EstiloMinecraft.aplicarBoton(boton);
        }

        barra.add(botonNuevo);
        barra.add(botonAbrir);
        barra.add(botonGuardar);
        barra.add(comboFuente);
        barra.add(comboTamanio);
        barra.add(botonColor);
        return barra;
    }

    private void cambiarFormato() {
        aplicarFormato();
        marcarCambios();
    }

    private void aplicarFormato() {
        String nombreFuente = (String) comboFuente.getSelectedItem();
        int tamanio = (Integer) comboTamanio.getSelectedItem();
        areaTexto.setFont(new Font(nombreFuente, Font.PLAIN, tamanio));
        areaTexto.setForeground(colorActual);
    }

    private void marcarCambios() {
        if (cargandoDocumento || cambiosSinGuardar) {
            return;
        }
        cambiosSinGuardar = true;
        actualizarTitulo();
    }

    private void actualizarTitulo() {
        String nombre = archivoActual == null ? "Sin titulo" : archivoActual.getName();
        setTitle("Editor de texto - " + nombre + (cambiosSinGuardar ? " *" : ""));
    }

    private void elegirColor() {
        Color elegido = JColorChooser.showDialog(this, "Color del texto", colorActual);
        if (elegido != null) {
            colorActual = elegido;
            cambiarFormato();
        }
    }

    public boolean confirmarCierre() {
        if (!cambiosSinGuardar) {
            return true;
        }
        String nombre = archivoActual == null ? "Sin titulo" : archivoActual.getName();
        int opcion = JOptionPane.showConfirmDialog(this, "¿Guardar los cambios de '" + nombre + "'?",
                "Editor de texto", JOptionPane.YES_NO_CANCEL_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            return guardarDocumento();
        }
        return opcion == JOptionPane.NO_OPTION;
    }

    private void nuevoDocumento() {
        if (!confirmarCierre()) {
            return;
        }
        cargandoDocumento = true;
        archivoActual = null;
        areaTexto.setText("");
        comboFuente.setSelectedIndex(0);
        comboTamanio.setSelectedItem(14);
        colorActual = Color.BLACK;
        aplicarFormato();
        cargandoDocumento = false;
        cambiosSinGuardar = false;
        actualizarTitulo();
    }

    private JFileChooser crearSelector() {
        JFileChooser selector = new JFileChooser(carpetaInicial);
        selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));
        return selector;
    }

    private void abrirDocumento() {
        if (!confirmarCierre()) {
            return;
        }
        JFileChooser selector = crearSelector();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File elegido = selector.getSelectedFile();
        if (!SistemaArchivos.estaDentroDe(elegido, raizPermitida)) {
            mostrarError("Solo puedes abrir archivos dentro de tu espacio en Mini-Windows.");
            return;
        }
        abrirArchivo(elegido);
    }

    public void abrirArchivo(File archivo) {
        if (!archivo.getName().toLowerCase().endsWith(".txt")) {
            mostrarError("Solo se pueden abrir archivos .txt");
            return;
        }
        DocumentoTexto documento;
        String texto;
        try {
            documento = ArchivoBinario.leerDocumentoTexto(archivo);
            texto = documento != null ? documento.getTexto() : ArchivoBinario.leerTextoPlano(archivo);
        } catch (ArchivoCorruptoException excepcion) {
            mostrarError(excepcion.getMessage());
            return;
        } catch (IOException excepcion) {
            mostrarError("No se pudo abrir el archivo: " + excepcion.getMessage());
            return;
        }
        cargandoDocumento = true;
        areaTexto.setText(texto);
        if (documento != null) {
            comboFuente.setSelectedItem(documento.getNombreFuente());
            comboTamanio.setSelectedItem(documento.getTamanioFuente());
            colorActual = new Color(documento.getColorRGB());
        } else {
            comboFuente.setSelectedIndex(0);
            comboTamanio.setSelectedItem(14);
            colorActual = Color.BLACK;
        }
        aplicarFormato();
        areaTexto.setCaretPosition(0);
        cargandoDocumento = false;
        archivoActual = archivo;
        cambiosSinGuardar = false;
        actualizarTitulo();
    }

    private boolean guardarDocumento() {
        if (archivoActual == null) {
            JFileChooser selector = crearSelector();
            if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            File elegido = selector.getSelectedFile();
            if (!elegido.getName().toLowerCase().endsWith(".txt")) {
                elegido = new File(elegido.getPath() + ".txt");
            }
            if (!SistemaArchivos.estaDentroDe(elegido, raizPermitida)) {
                mostrarError("Solo puedes guardar archivos dentro de tu espacio en Mini-Windows.");
                return false;
            }
            if (elegido.exists()) {
                int reemplazar = JOptionPane.showConfirmDialog(this, "'" + elegido.getName() + "' ya existe. ¿Deseas reemplazarlo?",
                        "Guardar", JOptionPane.YES_NO_OPTION);
                if (reemplazar != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            archivoActual = elegido;
        }
        if (SistemaArchivos.esArchivoDelSistema(archivoActual)) {
            mostrarError("'" + archivoActual.getName() + "' es un archivo del sistema, no se puede modificar.");
            return false;
        }
        String nombreFuente = (String) comboFuente.getSelectedItem();
        int tamanio = (Integer) comboTamanio.getSelectedItem();
        DocumentoTexto documento = new DocumentoTexto(areaTexto.getText(), nombreFuente, tamanio, colorActual.getRGB());
        try {
            ArchivoBinario.guardarObjeto(archivoActual.getPath(), documento);
        } catch (IOException excepcion) {
            mostrarError("Error al guardar: " + excepcion.getMessage());
            return false;
        }
        cambiosSinGuardar = false;
        actualizarTitulo();
        return true;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
