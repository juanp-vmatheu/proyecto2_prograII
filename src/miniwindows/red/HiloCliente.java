package miniwindows.red;

import miniwindows.GestorUsuarios;
import miniwindows.Usuario;
import miniwindows.excepciones.CredencialesInvalidasException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HiloCliente extends Thread {

    private Socket socket;
    private GestorUsuarios gestorUsuarios;
    private ServidorSOP servidor;

    public HiloCliente(Socket socket, GestorUsuarios gestorUsuarios, ServidorSOP servidor) {
        this.socket = socket;
        this.gestorUsuarios = gestorUsuarios;
        this.servidor = servidor;
    }

    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            String comando = entrada.readLine();
            servidor.registrarLog("Comando de " + socket.getInetAddress().getHostAddress() + ": " + comando);

            if (comando != null && comando.startsWith("LOGIN;")) {
                procesarLogin(comando, salida);
            } else {
                salida.println("ERROR;Comando no reconocido.");
            }

            socket.close();
        } catch (IOException excepcion) {
            servidor.registrarLog("Error con un cliente: " + excepcion.getMessage());
        }
    }

    private void procesarLogin(String comando, PrintWriter salida) {
        String[] partes = comando.split(";");
        if (partes.length < 3) {
            salida.println("ERROR;Formato invalido.");
            return;
        }
        String usuario = partes[1];
        String password = partes[2];
        synchronized (gestorUsuarios) {
            try {
                Usuario logueado = gestorUsuarios.validarCredenciales(usuario, password);
                salida.println("OK;" + logueado.getNombreCompleto() + ";" + logueado.isAdministrador()
                        + ";" + logueado.getNombreUsuario());
                servidor.registrarLog("Login exitoso: " + usuario);
            } catch (CredencialesInvalidasException excepcion) {
                salida.println("ERROR;" + excepcion.getMessage());
                servidor.registrarLog("Login fallido: " + usuario);
            }
        }
    }
}
