package insta;

import insta.ArchivoCorruptoException;
import insta.NucleoInsta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorInsta {

    public static final int PUERTO = 5500;

    public static void main(String[] args) throws ArchivoCorruptoException {
        iniciar();
    }

    public static void iniciar() throws ArchivoCorruptoException {
        NucleoInsta.inicializar();
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor INSTA+ escuchando en el puerto " + PUERTO);
            while (true) {
                Socket cliente = servidor.accept();
                new Thread(new ManejadorCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.out.println("No se pudo iniciar el servidor (puede que ya haya uno corriendo): " + e.getMessage());
        }
    }
}