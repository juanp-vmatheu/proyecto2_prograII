package miniwindows.archivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo));
        salida.writeObject(objeto);
        salida.close();
    }

    public static Object leerObjeto(String ruta) throws ArchivoCorruptoException {
        try {
            ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta));
            Object objeto = entrada.readObject();
            entrada.close();
            return objeto;
        } catch (IOException e) {
            throw new ArchivoCorruptoException("El archivo '" + ruta + "' esta danado o no se pudo leer.");
        } catch (ClassNotFoundException e) {
            throw new ArchivoCorruptoException("El archivo '" + ruta + "' no tiene el formato esperado.");
        }
    }

    public static boolean existeArchivo(String ruta) {
        File archivo = new File(ruta);
        return archivo.exists() && archivo.isFile();
    }
}
