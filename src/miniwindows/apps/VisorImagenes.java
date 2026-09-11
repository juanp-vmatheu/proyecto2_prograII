package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.hilos.HiloCargaImagenes;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class VisorImagenes extends JFrame {

    private static final int ANCHO_MAXIMO = 560;
    private static final int ALTO_MAXIMO = 380;

    private File raizNavegable;
    private File carpetaActual;
    private ListaEnlazada<File> imagenes;
    private int indiceActual;

    private JLabel etiquetaImagen;
    private JLabel etiquetaEstado;
    private JButton botonAnterior;
    private JButton botonSiguiente;
    private File archivoObjetivoInicial;
    private int solicitudActual;

    public VisorImagenes(Usuario usuarioActual) {
        super("Visor de imagenes");
        raizNavegable = usuarioActual.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        armarVentana();

        File carpetaPropia = SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        cargarCarpeta(new File(carpetaPropia, "Mis Imágenes"));
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        EstiloMinecraft.aplicarVentana(this);

        etiquetaImagen = new JLabel("", SwingConstants.CENTER);
        EstiloMinecraft.aplicarRanura(etiquetaImagen);
        etiquetaImagen.setOpaque(true);
        etiquetaImagen.setBackground(EstiloMinecraft.GRIS_RANURA);
        add(etiquetaImagen, BorderLayout.CENTER);

        etiquetaEstado = new JLabel("Cargando...", SwingConstants.CENTER);
        add(etiquetaEstado, BorderLayout.SOUTH);

        add(armarBarraHerramientas(), BorderLayout.NORTH);
    }

    private JPanel armarBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        EstiloMinecraft.aplicarPanel(barra);

        botonAnterior = new JButton("Anterior");
        botonAnterior.setEnabled(false);
        botonAnterior.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                mostrarAnterior();
            }
        });

        botonSiguiente = new JButton("Siguiente");
        botonSiguiente.setEnabled(false);
        botonSiguiente.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                mostrarSiguiente();
            }
        });

        JButton botonCambiarCarpeta = new JButton("Cambiar carpeta");
        botonCambiarCarpeta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cambiarCarpeta();
            }
        });

        JButton[] botonesBarra = {botonAnterior, botonSiguiente, botonCambiarCarpeta};
        for (JButton boton : botonesBarra) {
            EstiloMinecraft.aplicarBoton(boton);
        }

        barra.add(botonAnterior);
        barra.add(botonSiguiente);
        barra.add(botonCambiarCarpeta);
        return barra;
    }

    public void mostrarArchivo(File archivo) {
        archivoObjetivoInicial = archivo;
        cargarCarpeta(archivo.getParentFile());
    }

    private void cambiarCarpeta() {
        JFileChooser selector = new JFileChooser(raizNavegable);
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            cargarCarpeta(selector.getSelectedFile());
        }
    }

    private void cargarCarpeta(File carpeta) {
        carpetaActual = carpeta;
        final int idSolicitud = ++solicitudActual;
        etiquetaEstado.setText("Cargando imagenes...");
        botonAnterior.setEnabled(false);
        botonSiguiente.setEnabled(false);
        etiquetaImagen.setIcon(null);
        etiquetaImagen.setText("");
        HiloCargaImagenes hilo = new HiloCargaImagenes(carpeta, new HiloCargaImagenes.Callback() {
            public void alCargar(ListaEnlazada<File> imagenesCargadas) {
                if (idSolicitud == solicitudActual) {
                    aplicarImagenesCargadas(imagenesCargadas);
                }
            }
        });
        hilo.start();
    }

    private void aplicarImagenesCargadas(ListaEnlazada<File> imagenesCargadas) {
        imagenes = imagenesCargadas;
        indiceActual = 0;
        if (archivoObjetivoInicial != null) {
            for (int i = 0; i < imagenes.tamanio(); i++) {
                if (imagenes.obtener(i).equals(archivoObjetivoInicial)) {
                    indiceActual = i;
                    break;
                }
            }
            archivoObjetivoInicial = null;
        }
        if (imagenes.tamanio() == 0) {
            etiquetaEstado.setText("No hay imagenes en " + carpetaActual.getName());
            return;
        }
        botonAnterior.setEnabled(true);
        botonSiguiente.setEnabled(true);
        mostrarImagenActual();
    }

    private void mostrarImagenActual() {
        File archivo = imagenes.obtener(indiceActual);
        ImageIcon original = new ImageIcon(archivo.getPath());
        int anchoOriginal = original.getIconWidth();
        int altoOriginal = original.getIconHeight();
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            etiquetaImagen.setIcon(null);
            etiquetaImagen.setText("No se pudo cargar la imagen.");
            etiquetaEstado.setText((indiceActual + 1) + " de " + imagenes.tamanio() + " - " + archivo.getName());
            return;
        }
        double escala = Math.min((double) ANCHO_MAXIMO / anchoOriginal, (double) ALTO_MAXIMO / altoOriginal);
        int anchoFinal = (int) (anchoOriginal * escala);
        int altoFinal = (int) (altoOriginal * escala);
        Image escalada = original.getImage().getScaledInstance(anchoFinal, altoFinal, Image.SCALE_SMOOTH);
        etiquetaImagen.setText("");
        etiquetaImagen.setIcon(new ImageIcon(escalada));
        etiquetaEstado.setText((indiceActual + 1) + " de " + imagenes.tamanio() + " - " + archivo.getName());
    }

    private void mostrarAnterior() {
        if (imagenes == null || imagenes.tamanio() == 0) {
            return;
        }
        indiceActual = (indiceActual == 0) ? imagenes.tamanio() - 1 : indiceActual - 1;
        mostrarImagenActual();
    }

    private void mostrarSiguiente() {
        if (imagenes == null || imagenes.tamanio() == 0) {
            return;
        }
        indiceActual = (indiceActual + 1) % imagenes.tamanio();
        mostrarImagenActual();
    }
}
