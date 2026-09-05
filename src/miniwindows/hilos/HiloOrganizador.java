package miniwindows.hilos;

import javax.swing.SwingUtilities;
import java.io.File;

public class HiloOrganizador extends Thread {

    private static final Object CANDADO = new Object();

    private File carpeta;
    private Runnable alTerminar;

    public HiloOrganizador(File carpeta, Runnable alTerminar) {
        this.carpeta = carpeta;
        this.alTerminar = alTerminar;
    }

    public void run() {
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile()) {
                    clasificar(archivo);
                }
            }
        }
        SwingUtilities.invokeLater(alTerminar);
    }

    private void clasificar(File archivo) {
        String nombreCarpeta = obtenerCarpetaDestino(archivo.getName());
        if (nombreCarpeta == null) {
            return;
        }
        synchronized (CANDADO) {
            File carpetaDestino = new File(carpeta, nombreCarpeta);
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdirs();
            }
            File destino = new File(carpetaDestino, archivo.getName());
            if (!destino.exists()) {
                archivo.renameTo(destino);
            }
        }
    }

    private String obtenerCarpetaDestino(String nombreArchivo) {
        String nombreMinuscula = nombreArchivo.toLowerCase();
        if (nombreMinuscula.endsWith(".jpg") || nombreMinuscula.endsWith(".jpeg")
                || nombreMinuscula.endsWith(".png") || nombreMinuscula.endsWith(".gif")
                || nombreMinuscula.endsWith(".bmp")) {
            return "Imágenes";
        }
        if (nombreMinuscula.endsWith(".txt") || nombreMinuscula.endsWith(".pdf")
                || nombreMinuscula.endsWith(".doc") || nombreMinuscula.endsWith(".docx")) {
            return "Documentos";
        }
        if (nombreMinuscula.endsWith(".mp3") || nombreMinuscula.endsWith(".wav")) {
            return "Música";
        }
        return null;
    }
}
