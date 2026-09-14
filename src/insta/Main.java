package insta;

import insta.VentanaInsta;
import insta.ServidorInsta;  

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        if (host.equals("localhost")) {
            Thread hiloServidor = new Thread(() -> {
                try {
                    ServidorInsta.iniciar();
                } catch (Exception e) {
                    System.out.println("Ya hay un servidor INSTA+ corriendo, este cliente se conecta a el.");
                }
            });
            hiloServidor.setDaemon(true);
            hiloServidor.start();
        }

        SwingUtilities.invokeLater(() -> new VentanaInsta(host).setVisible(true));
    }
}