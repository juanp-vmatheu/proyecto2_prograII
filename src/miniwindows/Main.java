package miniwindows;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                VentanaLogin login = new VentanaLogin();
                login.setVisible(true);
            }
        });
    }
}
