package miniwindows;

import java.io.File;

public class SistemaArchivos {

    private static final String NOMBRE_RAIZ = "Z";

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
}
