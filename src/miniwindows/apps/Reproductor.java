package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.archivos.ArchivoBinario;
import miniwindows.archivos.Cancion;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.excepciones.ArchivoCorruptoException;
import miniwindows.hilos.HiloReproductor;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class Reproductor extends JFrame {

    private static final int ANCHO_CARATULA = 160;
    private static final int ALTO_CARATULA = 160;

    private File raizNavegable;
    private File carpetaActual;
    private ListaEnlazada<File> canciones;
    private ListaEnlazada<Cancion> metadatos;

    private DefaultListModel<String> modeloLista;
    private JList<String> listaUI;
    private JLabel etiquetaCaratula;
    private JLabel etiquetaTitulo;
    private JLabel etiquetaArtista;
    private JTextArea areaDescripcion;
    private JButton botonPlay;
    private JButton botonPause;
    private JButton botonStop;

    private HiloReproductor hiloActual;
    private File archivoEnCurso;
    private boolean pausado;

    public Reproductor(Usuario usuarioActual) {
        super("Reproductor de musica");
        raizNavegable = usuarioActual.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        armarVentana();

        File carpetaInicial = usuarioActual.isAdministrador()
                ? raizNavegable
                : new File(raizNavegable, "Música");
        cargarCarpeta(carpetaInicial);
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evento) {
                detenerReproduccion();
            }
        });

        modeloLista = new DefaultListModel<String>();
        listaUI = new JList<String>(modeloLista);
        listaUI.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evento) {
                if (!evento.getValueIsAdjusting()) {
                    mostrarInfoSeleccionada();
                }
            }
        });
        add(new JScrollPane(listaUI), BorderLayout.WEST);

        add(armarPanelInfo(), BorderLayout.CENTER);
        add(armarBarraControles(), BorderLayout.SOUTH);
        add(armarBarraSuperior(), BorderLayout.NORTH);
    }

    private JPanel armarBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton botonCambiarCarpeta = new JButton("Cambiar carpeta");
        botonCambiarCarpeta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cambiarCarpeta();
            }
        });
        barra.add(botonCambiarCarpeta);
        return barra;
    }

    private JPanel armarPanelInfo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        etiquetaCaratula = new JLabel("", SwingConstants.CENTER);
        etiquetaCaratula.setPreferredSize(new Dimension(ANCHO_CARATULA, ALTO_CARATULA));
        etiquetaCaratula.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(etiquetaCaratula, BorderLayout.WEST);

        JPanel panelTexto = new JPanel();
        panelTexto.setLayout(new BoxLayout(panelTexto, BoxLayout.Y_AXIS));
        etiquetaTitulo = new JLabel("Selecciona una cancion");
        etiquetaTitulo.setFont(etiquetaTitulo.getFont().deriveFont(Font.BOLD, 16f));
        etiquetaArtista = new JLabel("");
        areaDescripcion = new JTextArea(4, 24);
        areaDescripcion.setEditable(false);
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);

        JButton botonEditarInfo = new JButton("Editar info");
        botonEditarInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                editarInfoSeleccionada();
            }
        });

        panelTexto.add(etiquetaTitulo);
        panelTexto.add(etiquetaArtista);
        panelTexto.add(new JScrollPane(areaDescripcion));
        panelTexto.add(botonEditarInfo);
        panel.add(panelTexto, BorderLayout.CENTER);

        return panel;
    }

    private JPanel armarBarraControles() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        botonPlay = new JButton("Play");
        botonPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                reproducirSeleccionada();
            }
        });

        botonPause = new JButton("Pause");
        botonPause.setEnabled(false);
        botonPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                alternarPausa();
            }
        });

        botonStop = new JButton("Stop");
        botonStop.setEnabled(false);
        botonStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                detenerReproduccion();
            }
        });

        barra.add(botonPlay);
        barra.add(botonPause);
        barra.add(botonStop);
        return barra;
    }

    private void cambiarCarpeta() {
        JFileChooser selector = new JFileChooser(raizNavegable);
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            detenerReproduccion();
            cargarCarpeta(selector.getSelectedFile());
        }
    }

    private void cargarCarpeta(File carpeta) {
        carpetaActual = carpeta;
        canciones = new ListaEnlazada<File>();
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && archivo.getName().toLowerCase().endsWith(".mp3")) {
                    canciones.insertarOrdenado(archivo, SistemaArchivos.comparadorPorNombre());
                }
            }
        }
        cargarMetadatos();
        modeloLista.clear();
        for (int i = 0; i < canciones.tamanio(); i++) {
            modeloLista.addElement(obtenerNombreMostrado(canciones.obtener(i)));
        }
        limpiarPanelInfo();
    }

    private String obtenerRutaMetadatos() {
        return new File(carpetaActual, "canciones.sop").getPath();
    }

    private void cargarMetadatos() {
        metadatos = new ListaEnlazada<Cancion>();
        String ruta = obtenerRutaMetadatos();
        if (ArchivoBinario.existeArchivo(ruta)) {
            try {
                Object leido = ArchivoBinario.leerObjeto(ruta);
                metadatos = (ListaEnlazada<Cancion>) leido;
            } catch (ArchivoCorruptoException excepcion) {
                metadatos = new ListaEnlazada<Cancion>();
            }
        }
    }

    private void guardarMetadatos() {
        try {
            ArchivoBinario.guardarObjeto(obtenerRutaMetadatos(), metadatos);
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(this, "Error al guardar la informacion: " + excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Cancion buscarMetadato(String nombreArchivo) {
        for (int i = 0; i < metadatos.tamanio(); i++) {
            Cancion cancion = metadatos.obtener(i);
            if (cancion.getNombreArchivo().equals(nombreArchivo)) {
                return cancion;
            }
        }
        return null;
    }

    private String obtenerNombreMostrado(File archivo) {
        Cancion metadato = buscarMetadato(archivo.getName());
        if (metadato != null && metadato.getTitulo() != null && !metadato.getTitulo().isEmpty()) {
            return metadato.getTitulo();
        }
        return archivo.getName();
    }

    private void mostrarInfoSeleccionada() {
        int indice = listaUI.getSelectedIndex();
        if (indice == -1) {
            limpiarPanelInfo();
            return;
        }
        File archivo = canciones.obtener(indice);
        Cancion metadato = buscarMetadato(archivo.getName());
        if (metadato == null) {
            etiquetaTitulo.setText(archivo.getName());
            etiquetaArtista.setText("");
            areaDescripcion.setText("");
            etiquetaCaratula.setIcon(null);
            return;
        }
        etiquetaTitulo.setText(metadato.getTitulo() != null && !metadato.getTitulo().isEmpty() ? metadato.getTitulo() : archivo.getName());
        etiquetaArtista.setText(metadato.getArtista() == null ? "" : metadato.getArtista());
        areaDescripcion.setText(metadato.getDescripcion() == null ? "" : metadato.getDescripcion());
        mostrarCaratula(metadato.getRutaCaratula());
    }

    private void mostrarCaratula(String rutaCaratula) {
        if (rutaCaratula == null || rutaCaratula.isEmpty()) {
            etiquetaCaratula.setIcon(null);
            return;
        }
        ImageIcon original = new ImageIcon(rutaCaratula);
        if (original.getIconWidth() <= 0) {
            etiquetaCaratula.setIcon(null);
            return;
        }
        Image escalada = original.getImage().getScaledInstance(ANCHO_CARATULA, ALTO_CARATULA, Image.SCALE_SMOOTH);
        etiquetaCaratula.setIcon(new ImageIcon(escalada));
    }

    private void limpiarPanelInfo() {
        etiquetaTitulo.setText("Selecciona una cancion");
        etiquetaArtista.setText("");
        areaDescripcion.setText("");
        etiquetaCaratula.setIcon(null);
    }

    private void editarInfoSeleccionada() {
        int indice = listaUI.getSelectedIndex();
        if (indice == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cancion primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final File archivo = canciones.obtener(indice);
        Cancion existente = buscarMetadato(archivo.getName());

        final JTextField campoTitulo = new JTextField(existente != null ? existente.getTitulo() : archivo.getName());
        final JTextField campoArtista = new JTextField(existente != null ? existente.getArtista() : "");
        final JTextArea campoDescripcion = new JTextArea(existente != null ? existente.getDescripcion() : "", 3, 20);
        final String[] rutaCaratulaElegida = {existente != null ? existente.getRutaCaratula() : null};

        JButton botonElegirImagen = new JButton("Elegir caratula...");
        botonElegirImagen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                JFileChooser selector = new JFileChooser(raizNavegable);
                if (selector.showOpenDialog(Reproductor.this) == JFileChooser.APPROVE_OPTION) {
                    rutaCaratulaElegida[0] = selector.getSelectedFile().getPath();
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Titulo:"));
        panel.add(campoTitulo);
        panel.add(new JLabel("Artista:"));
        panel.add(campoArtista);
        panel.add(new JLabel("Descripcion:"));
        panel.add(new JScrollPane(campoDescripcion));
        panel.add(botonElegirImagen);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Editar informacion",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        Cancion actualizada = new Cancion(archivo.getName(), campoTitulo.getText().trim(),
                campoArtista.getText().trim(), campoDescripcion.getText().trim(), rutaCaratulaElegida[0]);
        if (existente != null) {
            metadatos.eliminar(existente);
        }
        metadatos.agregar(actualizada);
        guardarMetadatos();

        modeloLista.set(indice, obtenerNombreMostrado(archivo));
        mostrarInfoSeleccionada();
    }

    private void reproducirSeleccionada() {
        int indice = listaUI.getSelectedIndex();
        if (indice == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cancion primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        detenerHiloActual();
        iniciarReproduccion(canciones.obtener(indice));
    }

    private void iniciarReproduccion(File archivo) {
        archivoEnCurso = archivo;
        pausado = false;

        final HiloReproductor[] referenciaHilo = new HiloReproductor[1];
        HiloReproductor nuevoHilo = new HiloReproductor(archivo, new Runnable() {
            public void run() {
                if (hiloActual == referenciaHilo[0]) {
                    reproduccionTerminada();
                }
            }
        });
        referenciaHilo[0] = nuevoHilo;
        hiloActual = nuevoHilo;
        nuevoHilo.start();

        botonPlay.setEnabled(false);
        botonPause.setEnabled(true);
        botonPause.setText("Pause");
        botonStop.setEnabled(true);
    }

    private void alternarPausa() {
        if (pausado) {
            iniciarReproduccion(archivoEnCurso);
            return;
        }
        if (hiloActual == null) {
            return;
        }
        detenerHiloActual();
        pausado = true;
        botonPlay.setEnabled(false);
        botonPause.setEnabled(true);
        botonPause.setText("Reanudar");
        botonStop.setEnabled(true);
    }

    private void detenerHiloActual() {
        if (hiloActual != null) {
            hiloActual.detener();
            hiloActual = null;
        }
    }

    private void detenerReproduccion() {
        detenerHiloActual();
        archivoEnCurso = null;
        pausado = false;
        botonPlay.setEnabled(true);
        botonPause.setEnabled(false);
        botonPause.setText("Pause");
        botonStop.setEnabled(false);
    }

    private void reproduccionTerminada() {
        hiloActual = null;
        archivoEnCurso = null;
        pausado = false;
        botonPlay.setEnabled(true);
        botonPause.setEnabled(false);
        botonPause.setText("Pause");
        botonStop.setEnabled(false);
    }
}
