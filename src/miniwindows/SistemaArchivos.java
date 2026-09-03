package miniwindows;

import java.io.File;

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
}
