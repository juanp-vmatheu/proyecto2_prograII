package insta;

import insta.ArchivoCorruptoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GestorArchivos {

    private GestorArchivos() {
    }

    public static void guardar(String ruta, Object dato) throws ArchivoCorruptoException {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(dato);
        } catch (IOException e) {
            throw new ArchivoCorruptoException("No se pudo guardar el archivo: " + ruta, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cargar(String ruta) throws ArchivoCorruptoException {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ArchivoCorruptoException("El archivo esta dañado o corrupto: " + ruta, e);
        }
    }

    public static boolean existe(String ruta) {
        return new File(ruta).exists();
    }
}