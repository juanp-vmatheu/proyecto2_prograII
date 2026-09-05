package miniwindows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;

public class SistemaArchivos {

    private static final String NOMBRE_RAIZ = "Z";
    private static final String[] CARPETAS_BASE = {"Mis Documentos", "Música", "Mis Imágenes"};

    public static File obtenerRaiz() {
        File raiz = new File(NOMBRE_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
        return raiz;
    }

    public static String obtenerRutaArchivoUsuarios() {
        return obtenerRaiz().getPath() + File.separator + "usuarios.sop";
    }

    public static File obtenerCarpetaUsuario(String nombreUsuario) {
        return new File(obtenerRaiz(), nombreUsuario);
    }

    public static void crearCarpetasBase(String nombreUsuario) {
        File carpetaUsuario = obtenerCarpetaUsuario(nombreUsuario);
        carpetaUsuario.mkdirs();
        for (String nombreCarpeta : CARPETAS_BASE) {
            new File(carpetaUsuario, nombreCarpeta).mkdirs();
        }
    }

    public static boolean nombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        if (nombre.contains("..") || nombre.equals(".")) {
            return false;
        }
        if (nombre.contains("/") || nombre.contains("\\")) {
            return false;
        }
        if (new File(nombre).isAbsolute()) {
            return false;
        }
        return true;
    }

    public static boolean eliminarRecursivo(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivo(hijo)) {
                        return false;
                    }
                }
            }
        }
        return archivo.delete();
    }

    public static long tamanoRecursivo(File archivo) {
        if (archivo.isFile()) {
            return archivo.length();
        }
        long total = 0;
        File[] hijos = archivo.listFiles();
        if (hijos != null) {
            for (File hijo : hijos) {
                total += tamanoRecursivo(hijo);
            }
        }
        return total;
    }

    public static void copiarArchivo(File origen, File destino) throws IOException {
        FileInputStream entrada = new FileInputStream(origen);
        FileOutputStream salida = new FileOutputStream(destino);
        byte[] buffer = new byte[4096];
        int leidos;
        while ((leidos = entrada.read(buffer)) != -1) {
            salida.write(buffer, 0, leidos);
        }
        entrada.close();
        salida.close();
    }

    public static void copiarCarpetaRecursivo(File origen, File destino) throws IOException {
        destino.mkdirs();
        File[] hijos = origen.listFiles();
        if (hijos == null) {
            return;
        }
        for (File hijo : hijos) {
            File destinoHijo = new File(destino, hijo.getName());
            if (hijo.isDirectory()) {
                copiarCarpetaRecursivo(hijo, destinoHijo);
            } else {
                copiarArchivo(hijo, destinoHijo);
            }
        }
    }

    public static Comparator<File> comparadorPorNombre() {
        return new Comparator<File>() {
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        };
    }
}
