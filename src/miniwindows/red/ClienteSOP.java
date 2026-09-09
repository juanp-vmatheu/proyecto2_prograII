package miniwindows.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteSOP {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    private static final int TIEMPO_ESPERA_MS = 1000;

    public static String[] login(String usuario, String password) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(HOST, PUERTO), TIEMPO_ESPERA_MS);
        try {
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida.println("LOGIN;" + usuario + ";" + password);
            String respuesta = entrada.readLine();
            if (respuesta == null) {
                throw new IOException("El servidor no respondio.");
            }
            return respuesta.split(";");
        } finally {
            socket.close();
        }
    }
}
