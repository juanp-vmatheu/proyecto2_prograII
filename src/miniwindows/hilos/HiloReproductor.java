package miniwindows.hilos;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HiloReproductor extends Thread {

    private File archivo;
    private Runnable alTerminar;
    private volatile AdvancedPlayer reproductor;

    public HiloReproductor(File archivo, Runnable alTerminar) {
        this.archivo = archivo;
        this.alTerminar = alTerminar;
    }

    public void run() {
        try {
            FileInputStream entrada = new FileInputStream(archivo);
            reproductor = new AdvancedPlayer(entrada);
            reproductor.play();
        } catch (JavaLayerException excepcion) {
            System.out.println("Error al reproducir: " + excepcion.getMessage());
        } catch (IOException excepcion) {
            System.out.println("Error al reproducir: " + excepcion.getMessage());
        } finally {
            SwingUtilities.invokeLater(alTerminar);
        }
    }

    public void detener() {
        if (reproductor != null) {
            reproductor.close();
        }
    }
}
