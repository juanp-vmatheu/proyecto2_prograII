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
        void alTerminar(long posicionFinalBytes, boolean fuePausa, String error);
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
        int bitrate = obtenerBitrate(archivo);
        if (bitrate <= 0) {
            avisarFin(posicionInicialBytes, false, "'" + archivo.getName() + "' no es un archivo mp3 valido.");
            return;
        }
        double bytesPorMs = bitrate / 8.0 / 1000.0;
        long tiempoInicio = 0;
        FileInputStream entrada = null;
        String error = null;
        try {
            entrada = new FileInputStream(archivo);
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
            error = "No se pudo reproducir '" + archivo.getName() + "': " + excepcion.getMessage();
        } catch (IOException excepcion) {
            error = "No se pudo reproducir '" + archivo.getName() + "': " + excepcion.getMessage();
        } finally {
            if (reproductor != null) {
                reproductor.close();
            } else {
                cerrar(entrada);
            }
            long tiempoTranscurrido = tiempoInicio == 0 ? 0 : System.currentTimeMillis() - tiempoInicio;
            avisarFin(posicionInicialBytes + (long) (bytesPorMs * tiempoTranscurrido), detenidoPorPausa, error);
        }
    }

    private void avisarFin(final long posicionFinal, final boolean fuePausa, final String error) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                alTerminar.alTerminar(posicionFinal, fuePausa, error);
            }
        });
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
        FileInputStream entradaPeek = null;
        try {
            entradaPeek = new FileInputStream(archivo);
            Bitstream bitstreamPeek = new Bitstream(entradaPeek);
            Header encabezado = bitstreamPeek.readFrame();
            return encabezado != null ? encabezado.bitrate() : -1;
        } catch (Exception excepcion) {
            return BITRATE_POR_DEFECTO;
        } finally {
            cerrar(entradaPeek);
        }
    }

    private static void cerrar(FileInputStream entrada) {
        if (entrada == null) {
            return;
        }
        try {
            entrada.close();
        } catch (IOException excepcion) {
            System.out.println("No se pudo cerrar el archivo: " + excepcion.getMessage());
        }
    }
}
