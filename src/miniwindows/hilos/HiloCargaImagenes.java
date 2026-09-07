package miniwindows.hilos;

import miniwindows.SistemaArchivos;
import miniwindows.estructuras.ListaEnlazada;

import javax.swing.SwingUtilities;
import java.io.File;

public class HiloCargaImagenes extends Thread {

    public interface Callback {
        void alCargar(ListaEnlazada<File> imagenes);
    }

    private File carpeta;
    private Callback callback;

    public HiloCargaImagenes(File carpeta, Callback callback) {
        this.carpeta = carpeta;
        this.callback = callback;
    }

    public void run() {
        final ListaEnlazada<File> imagenes = new ListaEnlazada<File>();
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && esImagen(archivo.getName())) {
                    imagenes.insertarOrdenado(archivo, SistemaArchivos.comparadorPorNombre());
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                callback.alCargar(imagenes);
            }
        });
    }

    private boolean esImagen(String nombreArchivo) {
        String nombreMinuscula = nombreArchivo.toLowerCase();
        return nombreMinuscula.endsWith(".jpg") || nombreMinuscula.endsWith(".jpeg")
                || nombreMinuscula.endsWith(".png") || nombreMinuscula.endsWith(".gif")
                || nombreMinuscula.endsWith(".bmp");
    }
}
