package miniwindows.archivos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import miniwindows.excepciones.ArchivoCorruptoException;

public class ArchivoBinario {

    public static void guardarObjeto(String ruta, Serializable objeto) throws IOException {
        File archivo = new File(ruta);
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            carpetaPadre.mkdirs();
        }
        FileOutputStream archivoSalida = new FileOutputStream(archivo);
        try {
            ObjectOutputStream salida = new ObjectOutputStream(archivoSalida);
            salida.writeObject(objeto);
            salida.flush();
        } finally {
            archivoSalida.close();
        }
    }

    public static Object leerObjeto(String ruta) throws ArchivoCorruptoException {
        FileInputStream archivoEntrada = null;
        try {
            archivoEntrada = new FileInputStream(ruta);
            ObjectInputStream entrada = new ObjectInputStream(archivoEntrada);
            return entrada.readObject();
        } catch (IOException e) {
            throw new ArchivoCorruptoException("El archivo '" + ruta + "' esta danado o no se pudo leer.");
        } catch (ClassNotFoundException e) {
            throw new ArchivoCorruptoException("El archivo '" + ruta + "' no tiene el formato esperado.");
        } finally {
            cerrar(archivoEntrada);
        }
    }

    public static DocumentoTexto leerDocumentoTexto(File archivo) throws ArchivoCorruptoException {
        if (!esArchivoSerializado(archivo)) {
            return null;
        }
        Object leido = leerObjeto(archivo.getPath());
        if (!(leido instanceof DocumentoTexto)) {
            throw new ArchivoCorruptoException("El archivo '" + archivo.getName() + "' no es un documento de texto.");
        }
        return (DocumentoTexto) leido;
    }

    public static String leerTextoPlano(File archivo) throws IOException {
        StringBuilder texto = new StringBuilder();
        BufferedReader lector = new BufferedReader(new FileReader(archivo));
        try {
            String linea;
            boolean primera = true;
            while ((linea = lector.readLine()) != null) {
                if (!primera) {
                    texto.append("\n");
                }
                texto.append(linea);
                primera = false;
            }
        } finally {
            lector.close();
        }
        return texto.toString();
    }

    private static boolean esArchivoSerializado(File archivo) {
        if (!archivo.isFile() || archivo.length() < 2) {
            return false;
        }
        FileInputStream entrada = null;
        try {
            entrada = new FileInputStream(archivo);
            return entrada.read() == 0xAC && entrada.read() == 0xED;
        } catch (IOException e) {
            return false;
        } finally {
            cerrar(entrada);
        }
    }

    private static void cerrar(FileInputStream entrada) {
        if (entrada == null) {
            return;
        }
        try {
            entrada.close();
        } catch (IOException e) {
            System.out.println("No se pudo cerrar el archivo: " + e.getMessage());
        }
    }

    public static boolean existeArchivo(String ruta) {
        File archivo = new File(ruta);
        return archivo.exists() && archivo.isFile();
    }
}
