package miniwindows.red;

import javax.swing.SwingUtilities;

public class MainServidor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServidorSOP servidor = new ServidorSOP();
                servidor.setVisible(true);
                servidor.iniciarServidor();
            }
        });
    }
}
