package miniwindows;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

public class PanelFondo extends JPanel {

    private Image fondo;

    public PanelFondo(String rutaFondo) {
        File archivo = new File(rutaFondo);
        if (archivo.exists()) {
            fondo = new ImageIcon(archivo.getPath()).getImage();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
