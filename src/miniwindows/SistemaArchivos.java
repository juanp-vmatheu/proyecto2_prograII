package miniwindows;

import miniwindows.archivos.ArchivoBinario;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.excepciones.ArchivoCorruptoException;
import miniwindows.excepciones.CarpetaNoEncontradaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;

public class SistemaArchivos {

    private static final String NOMBRE_RAIZ = "Z";
    private static final String[] CARPETAS_BASE = {"Mis Documentos", "Música", "Mis Imágenes"};
    private static final String[] NOMBRES_DEL_SISTEMA = {"usuarios.sop", "INSTA_RAIZ"};

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
        if (nombre.contains(":") || nombre.contains("*") || nombre.contains("?") || nombre.contains("\"")
                || nombre.contains("<") || nombre.contains(">") || nombre.contains("|")) {
            return false;
        }
        if (new File(nombre).isAbsolute()) {
            return false;
        }
        return true;
    }

    public static boolean nombreUsuarioValido(String nombreUsuario) {
        return nombreUsuario != null && nombreUsuario.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)*");
    }

    public static boolean esArchivoDelSistema(File archivo) {
        File elementoEnRaiz = obtenerElementoEnRaiz(archivo);
        if (elementoEnRaiz == null) {
            return false;
        }
        for (String nombreSistema : NOMBRES_DEL_SISTEMA) {
            if (elementoEnRaiz.getName().equalsIgnoreCase(nombreSistema)) {
                return true;
            }
        }
        return false;
    }

    public static boolean esProtegido(File archivo) {
        if (esArchivoDelSistema(archivo)) {
            return true;
        }
        File elementoEnRaiz = obtenerElementoEnRaiz(archivo);
        return elementoEnRaiz != null && elementoEnRaiz.equals(archivo.getAbsoluteFile())
                && existeUsuario(elementoEnRaiz.getName());
    }

    private static File obtenerElementoEnRaiz(File archivo) {
        File raiz = obtenerRaiz().getAbsoluteFile();
        File actual = archivo.getAbsoluteFile();
        while (actual != null) {
            File padre = actual.getParentFile();
            if (raiz.equals(padre)) {
                return actual;
            }
            actual = padre;
        }
        return null;
    }

    private static boolean existeUsuario(String nombreUsuario) {
        String ruta = obtenerRutaArchivoUsuarios();
        if (!ArchivoBinario.existeArchivo(ruta)) {
            return false;
        }
        try {
            ListaEnlazada<Usuario> usuarios = (ListaEnlazada<Usuario>) ArchivoBinario.leerObjeto(ruta);
            for (int i = 0; i < usuarios.tamanio(); i++) {
                if (usuarios.obtener(i).getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                    return true;
                }
            }
        } catch (ArchivoCorruptoException excepcion) {
            return false;
        }
        return false;
    }

    public static boolean estaDentroDe(File archivo, File carpeta) {
        try {
            String rutaArchivo = archivo.getCanonicalPath();
            String rutaCarpeta = carpeta.getCanonicalPath();
            return rutaArchivo.equals(rutaCarpeta) || rutaArchivo.startsWith(rutaCarpeta + File.separator);
        } catch (IOException excepcion) {
            return false;
        }
    }

    public static void verificarCarpeta(File carpeta) throws CarpetaNoEncontradaException {
        if (carpeta == null || !carpeta.isDirectory()) {
            String nombre = carpeta == null ? "" : carpeta.getName();
            throw new CarpetaNoEncontradaException("La carpeta '" + nombre + "' no existe.");
        }
    }

    public static File nombreDeCopiaDisponible(File carpetaDestino, File original) {
        File destino = new File(carpetaDestino, original.getName());
        if (!destino.exists()) {
            return destino;
        }
        String base = original.getName();
        String extension = "";
        int punto = base.lastIndexOf('.');
        if (!original.isDirectory() && punto > 0) {
            extension = base.substring(punto);
            base = base.substring(0, punto);
        }
        destino = new File(carpetaDestino, base + " - copia" + extension);
        int numero = 2;
        while (destino.exists()) {
            destino = new File(carpetaDestino, base + " - copia (" + numero + ")" + extension);
            numero++;
        }
        return destino;
    }

    public static String firmaArchivos(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return "";
        }
        StringBuilder firma = new StringBuilder();
        for (File archivo : archivos) {
            firma.append(archivo.getName()).append('|').append(archivo.lastModified())
                    .append('|').append(archivo.length()).append(';');
        }
        return firma.toString();
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
        FileOutputStream salida = null;
        try {
            salida = new FileOutputStream(destino);
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
        } finally {
            entrada.close();
            if (salida != null) {
                salida.close();
            }
        }
    }

    public static void copiarCarpetaRecursivo(File origen, File destino) throws IOException {
        File[] hijos = origen.listFiles();
        destino.mkdirs();
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

    public static Comparator<File> comparadorPorFecha() {
        return new Comparator<File>() {
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                return Long.compare(b.lastModified(), a.lastModified());
            }
        };
    }

    public static Comparator<File> comparadorPorTipo() {
        return new Comparator<File>() {
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                int comparacion = obtenerExtension(a).compareToIgnoreCase(obtenerExtension(b));
                if (comparacion != 0) {
                    return comparacion;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        };
    }

    public static Comparator<File> comparadorPorTamano() {
        return new Comparator<File>() {
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                if (a.isDirectory()) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
                return Long.compare(a.length(), b.length());
            }
        };
    }

    private static String obtenerExtension(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto == -1 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto + 1);
    }
}
