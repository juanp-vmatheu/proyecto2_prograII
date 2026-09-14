package insta;

import insta.Protocolo;
import insta.Respuesta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClienteInsta {

    private final String host;
    private final int puerto;

    public ClienteInsta(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public Respuesta enviar(String comando) {
        try (Socket socket = new Socket(host, puerto)) {
            BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            salida.write(comando);
            salida.newLine();
            salida.flush();
            socket.shutdownOutput();

            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            return (Respuesta) entrada.readObject();

        } catch (IOException | ClassNotFoundException e) {
            return Respuesta.error("No se pudo conectar con el servidor: " + e.getMessage());
        }
    }

    public String armar(String comando, String... partes) {
        StringBuilder sb = new StringBuilder(comando);
        for (String parte : partes) {
            sb.append(Protocolo.SEPARADOR).append(parte == null ? "" : parte);
        }
        return sb.toString();
    }
}