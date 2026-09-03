package miniwindows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
        FileReader lector = new FileReader(origen);
        FileWriter escritor = new FileWriter(destino);
        char[] buffer = new char[1024];
        int leidos;
        while ((leidos = lector.read(buffer)) != -1) {
            escritor.write(buffer, 0, leidos);
        }
        lector.close();
        escritor.close();
    }
}
