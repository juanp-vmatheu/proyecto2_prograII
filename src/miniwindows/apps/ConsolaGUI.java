package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ConsolaGUI extends JFrame {

    private static final Color FONDO = new Color(12, 12, 12);
    private static final Color TEXTO = new Color(220, 220, 220);
    private static final Font FUENTE = new Font("Consolas", Font.PLAIN, 14);

    private JTextArea salida;
    private JTextField entrada;
    private JLabel etiquetaPrompt;
    private String rutaActual;
    private ArrayList<String> historial;
    private int indiceHistorial;
    private Comandos_logica logica;

    public ConsolaGUI(Usuario usuario) {
        super("Simulador de Consola de Comandos");
        File raizConsola = usuario.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuario.getNombreUsuario());
        logica = new Comandos_logica(raizConsola);
        rutaActual = rutaCorta(logica.prompt());
        historial = new ArrayList<String>();
        indiceHistorial = 0;
        armarVentana();
        mostrarBanner();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 520);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        salida = new JTextArea();
        salida.setEditable(false);
        salida.setBackground(FONDO);
        salida.setForeground(TEXTO);
        salida.setFont(FUENTE);
        salida.setLineWrap(false);
        salida.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(salida);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO);
        add(scroll, BorderLayout.CENTER);

        etiquetaPrompt = new JLabel(rutaActual + ">");
        etiquetaPrompt.setForeground(TEXTO);
        etiquetaPrompt.setFont(FUENTE);

        entrada = new JTextField();
        entrada.setBackground(FONDO);
        entrada.setForeground(TEXTO);
        entrada.setCaretColor(TEXTO);
        entrada.setFont(FUENTE);
        entrada.setBorder(null);
        entrada.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                leerEntrada();
            }
        });
        entrada.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evento) {
                if (evento.getKeyCode() == KeyEvent.VK_UP) {
                    mostrarComandoAnterior();
                }
                if (evento.getKeyCode() == KeyEvent.VK_DOWN) {
                    mostrarComandoSiguiente();
                }
            }
        });

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBackground(FONDO);
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelEntrada.add(etiquetaPrompt, BorderLayout.WEST);
        panelEntrada.add(entrada, BorderLayout.CENTER);
        add(panelEntrada, BorderLayout.SOUTH);
    }

    public void mostrarVentana() {
        setVisible(true);
        entrada.requestFocusInWindow();
    }

    private void leerEntrada() {
        boolean escribiendo = estaEscribiendo();
        String comando;
        if (escribiendo) {
            comando = entrada.getText();
        } else {
            comando = entrada.getText().trim();
        }
        salida.append(etiquetaPrompt.getText() + comando + "\n");
        if (escribiendo || comando.length() > 0) {
            if (!escribiendo) {
                historial.add(comando);
                indiceHistorial = historial.size();
            }
            String resultado = procesar(comando);
            if (resultado.length() > 0) {
                salida.append(resultado + "\n");
            }
        }
        entrada.setText("");
        salida.setCaretPosition(salida.getDocument().getLength());
        entrada.requestFocusInWindow();
    }

    private boolean estaEscribiendo() {
        return logica.prompt().length() == 0;
    }

    private void mostrarComandoAnterior() {
        if (historial.size() > 0) {
            if (indiceHistorial > 0) {
                indiceHistorial--;
            }
            entrada.setText(historial.get(indiceHistorial));
            entrada.setCaretPosition(entrada.getText().length());
        }
    }

    private void mostrarComandoSiguiente() {
        if (historial.size() > 0) {
            if (indiceHistorial < historial.size() - 1) {
                indiceHistorial++;
                entrada.setText(historial.get(indiceHistorial));
            } else {
                indiceHistorial = historial.size();
                entrada.setText("");
            }
            entrada.setCaretPosition(entrada.getText().length());
        }
    }

    private void mostrarBanner() {
        salida.append("Simulador de Consola de Comandos [Version 1.0]\n");
        salida.append("Mini-Windows - Proyecto II\n\n");
        salida.append("Escriba Help para ver la lista de comandos.\n\n");
    }

    private String procesar(String comando) {
        String resultado = logica.ejecutar(comando);
        setPrompt(rutaCorta(logica.prompt()));
        if (resultado.equals(Comandos_logica.LIMPIAR)) {
            salida.setText("");
            return "";
        }
        if (resultado.equals(Comandos_logica.SALIR)) {
            dispose();
        }
        return resultado;
    }

    private String rutaCorta(String rutaAbsoluta) {
        String raizAbsoluta = SistemaArchivos.obtenerRaiz().getAbsolutePath();
        if (rutaAbsoluta.startsWith(raizAbsoluta)) {
            return "Z:" + rutaAbsoluta.substring(raizAbsoluta.length());
        }
        return rutaAbsoluta;
    }

    public void setPrompt(String ruta) {
        rutaActual = ruta;
        etiquetaPrompt.setText(rutaActual + ">");
    }
}
