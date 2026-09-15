package insta;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CargadorIconos {

    private static final Map<String, ImageIcon> ICONOS_CARGADOS = new HashMap<>();

    private CargadorIconos() {
    }

    public static synchronized ImageIcon cargarEscalado(String ruta, int tamano) {
        if (ruta == null || ruta.isEmpty()) {
            return null;
        }
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return null;
        }
        String clave = archivo.getAbsolutePath() + "|" + tamano + "|" + archivo.lastModified();
        ImageIcon iconoGuardado = ICONOS_CARGADOS.get(clave);
        if (iconoGuardado != null) {
            return iconoGuardado;
        }
        BufferedImage original;
        try {
            original = ImageIO.read(archivo);
        } catch (IOException e) {
            return null;
        }
        if (original == null) {
            return null;
        }
        Image escalada;
        if (original.getWidth() >= original.getHeight()) {
            escalada = original.getScaledInstance(tamano, -1, Image.SCALE_SMOOTH);
        } else {
            escalada = original.getScaledInstance(-1, tamano, Image.SCALE_SMOOTH);
        }
        ImageIcon icono = new ImageIcon(escalada);
        ICONOS_CARGADOS.put(clave, icono);
        return icono;
    }
}
