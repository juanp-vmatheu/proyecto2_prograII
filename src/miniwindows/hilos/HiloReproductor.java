package miniwindows.hilos;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HiloReproductor extends Thread {

    private static final int BITRATE_POR_DEFECTO = 128000;

    public interface Callback {
        void alTerminar(long posicionFinalBytes, boolean fuePausa);
    }

    private File archivo;
    private long posicionInicialBytes;
    private Callback alTerminar;
    private volatile AdvancedPlayer reproductor;
    private volatile boolean detenidoPorPausa;

    public HiloReproductor(File archivo, long posicionInicialBytes, Callback alTerminar) {
        this.archivo = archivo;
        this.posicionInicialBytes = posicionInicialBytes;
        this.alTerminar = alTerminar;
    }

    public void run() {
        double bytesPorMs = obtenerBitrate(archivo) / 8.0 / 1000.0;
        long tiempoInicio = 0;
        try {
            FileInputStream entrada = new FileInputStream(archivo);
            long saltados = 0;
            while (saltados < posicionInicialBytes) {
                long avance = entrada.skip(posicionInicialBytes - saltados);
                if (avance <= 0) {
                    break;
                }
                saltados += avance;
            }
            reproductor = new AdvancedPlayer(entrada);
            tiempoInicio = System.currentTimeMillis();
            reproductor.play();
        } catch (JavaLayerException excepcion) {
            System.out.println("Error al reproducir: " + excepcion.getMessage());
        } catch (IOException excepcion) {
            System.out.println("Error al reproducir: " + excepcion.getMessage());
        } finally {
            if (reproductor != null) {
                reproductor.close();
            }
            long tiempoTranscurrido = tiempoInicio == 0 ? 0 : System.currentTimeMillis() - tiempoInicio;
            final long posicionFinal = posicionInicialBytes + (long) (bytesPorMs * tiempoTranscurrido);
            final boolean fuePausa = detenidoPorPausa;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    alTerminar.alTerminar(posicionFinal, fuePausa);
                }
            });
        }
    }

    public void detener() {
        if (reproductor != null) {
            reproductor.close();
        }
    }

    public void pausar() {
        detenidoPorPausa = true;
        detener();
    }

    private static int obtenerBitrate(File archivo) {
        try {
            FileInputStream entradaPeek = new FileInputStream(archivo);
            Bitstream bitstreamPeek = new Bitstream(entradaPeek);
            Header encabezado = bitstreamPeek.readFrame();
            int bitrate = encabezado != null ? encabezado.bitrate() : BITRATE_POR_DEFECTO;
            bitstreamPeek.close();
            return bitrate;
        } catch (Exception excepcion) {
            return BITRATE_POR_DEFECTO;
        }
    }
}
