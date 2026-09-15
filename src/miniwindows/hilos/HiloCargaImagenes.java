package miniwindows.hilos;

import miniwindows.SistemaArchivos;
import miniwindows.estructuras.ListaEnlazada;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

public class HiloCargaImagenes extends Thread {

    public interface Callback {
        void alCargar(ListaEnlazada<File> imagenes, ListaEnlazada<ImageIcon> miniaturas);
    }

    private File carpeta;
    private int tamanioMiniatura;
    private Callback callback;

    public HiloCargaImagenes(File carpeta, int tamanioMiniatura, Callback callback) {
        this.carpeta = carpeta;
        this.tamanioMiniatura = tamanioMiniatura;
        this.callback = callback;
    }

    public void run() {
        final ListaEnlazada<File> imagenes = new ListaEnlazada<File>();
        final ListaEnlazada<ImageIcon> miniaturas = new ListaEnlazada<ImageIcon>();
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && esImagen(archivo.getName())) {
                    imagenes.insertarOrdenado(archivo, SistemaArchivos.comparadorPorNombre());
                }
            }
        }
        for (int i = 0; i < imagenes.tamanio(); i++) {
            miniaturas.agregar(crearMiniatura(imagenes.obtener(i)));
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                callback.alCargar(imagenes, miniaturas);
            }
        });
    }

    public static ImageIcon cargarSinCache(File archivo) {
        ImageIcon icono = new ImageIcon(Toolkit.getDefaultToolkit().createImage(archivo.getPath()));
        if (icono.getIconWidth() <= 0 || icono.getIconHeight() <= 0) {
            return null;
        }
        return icono;
    }

    private ImageIcon crearMiniatura(File archivo) {
        ImageIcon original = cargarSinCache(archivo);
        if (original == null) {
            return null;
        }
        Image escalada;
        if (original.getIconWidth() >= original.getIconHeight()) {
            escalada = original.getImage().getScaledInstance(tamanioMiniatura, -1, Image.SCALE_FAST);
        } else {
            escalada = original.getImage().getScaledInstance(-1, tamanioMiniatura, Image.SCALE_FAST);
        }
        return new ImageIcon(escalada);
    }

    private boolean esImagen(String nombreArchivo) {
        String nombreMinuscula = nombreArchivo.toLowerCase();
        return nombreMinuscula.endsWith(".jpg") || nombreMinuscula.endsWith(".jpeg")
                || nombreMinuscula.endsWith(".png") || nombreMinuscula.endsWith(".gif")
                || nombreMinuscula.endsWith(".bmp");
    }
}
