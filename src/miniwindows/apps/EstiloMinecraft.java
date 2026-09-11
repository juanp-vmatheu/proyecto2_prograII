package miniwindows.apps;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Font;

public class EstiloMinecraft {

    public static final Color GRIS_FONDO = new Color(198, 198, 198);
    public static final Color GRIS_CLARO = new Color(255, 255, 255);
    public static final Color GRIS_OSCURO = new Color(85, 85, 85);
    public static final Color GRIS_RANURA = new Color(139, 139, 139);
    public static final Font FUENTE = new Font("Monospaced", Font.BOLD, 12);

    public static void aplicarVentana(JFrame ventana) {
        ventana.getContentPane().setBackground(GRIS_FONDO);
    }

    public static void aplicarPanel(JPanel panel) {
        panel.setBackground(GRIS_FONDO);
    }

    public static void aplicarBoton(JButton boton) {
        boton.setBackground(GRIS_FONDO);
        boton.setFont(FUENTE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, GRIS_CLARO, GRIS_OSCURO));
    }

    public static void aplicarRanura(JComponent componente) {
        componente.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, GRIS_OSCURO, GRIS_CLARO));
    }
}
