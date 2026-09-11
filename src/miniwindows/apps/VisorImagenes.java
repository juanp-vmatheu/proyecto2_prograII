package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.hilos.HiloCargaImagenes;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class VisorImagenes extends JFrame {

    private static final int ANCHO_MAXIMO = 520;
    private static final int ALTO_MAXIMO = 340;
    private static final int TAMANIO_FLECHA = 36;
    private static final int TAMANIO_MINIATURA = 56;

    private File raizNavegable;
    private File carpetaActual;
    private ListaEnlazada<File> imagenes;
    private int indiceActual;

    private JLabel etiquetaImagen;
    private JLabel etiquetaEstado;
    private JButton botonAnterior;
    private JButton botonSiguiente;
    private JPanel panelMiniaturas;
    private JScrollPane scrollMiniaturas;
    private JButton[] botonesMiniatura;
    private File archivoObjetivoInicial;
    private int solicitudActual;

    public VisorImagenes(Usuario usuarioActual) {
        super("Visor de imagenes");
        raizNavegable = usuarioActual.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        armarVentana();

        File carpetaPropia = SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        cargarCarpeta(new File(carpetaPropia, "Mis Imágenes"));
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        EstiloMinecraft.aplicarVentana(this);

        etiquetaImagen = new JLabel("", SwingConstants.CENTER);
        EstiloMinecraft.aplicarRanura(etiquetaImagen);
        etiquetaImagen.setOpaque(true);
        etiquetaImagen.setBackground(EstiloMinecraft.GRIS_RANURA);

        JPanel panelCarrusel = new JPanel(new BorderLayout());
        EstiloMinecraft.aplicarPanel(panelCarrusel);

        botonAnterior = crearBotonFlecha(true);
        botonAnterior.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                mostrarAnterior();
            }
        });

        botonSiguiente = crearBotonFlecha(false);
        botonSiguiente.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                mostrarSiguiente();
            }
        });

        panelCarrusel.add(botonAnterior, BorderLayout.WEST);
        panelCarrusel.add(etiquetaImagen, BorderLayout.CENTER);
        panelCarrusel.add(botonSiguiente, BorderLayout.EAST);
        add(panelCarrusel, BorderLayout.CENTER);

        add(armarPanelInferior(), BorderLayout.SOUTH);
        add(armarBarraHerramientas(), BorderLayout.NORTH);
    }

    private JPanel armarPanelInferior() {
        JPanel panelInferior = new JPanel(new BorderLayout());
        EstiloMinecraft.aplicarPanel(panelInferior);

        etiquetaEstado = new JLabel("Cargando...", SwingConstants.CENTER);
        panelInferior.add(etiquetaEstado, BorderLayout.NORTH);

        panelMiniaturas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        EstiloMinecraft.aplicarPanel(panelMiniaturas);

        scrollMiniaturas = new JScrollPane(panelMiniaturas);
        scrollMiniaturas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollMiniaturas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollMiniaturas.setPreferredSize(new Dimension(100, TAMANIO_MINIATURA + 24));
        EstiloMinecraft.aplicarRanura(scrollMiniaturas);
        panelInferior.add(scrollMiniaturas, BorderLayout.SOUTH);

        return panelInferior;
    }

    private JButton crearBotonFlecha(boolean izquierda) {
        JButton boton = new JButton(new FlechaIcon(izquierda, TAMANIO_FLECHA));
        EstiloMinecraft.aplicarBoton(boton);
        boton.setPreferredSize(new Dimension(TAMANIO_FLECHA + 16, TAMANIO_FLECHA + 16));
        boton.setEnabled(false);
        return boton;
    }

    private JPanel armarBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        EstiloMinecraft.aplicarPanel(barra);

        JButton botonImportar = new JButton("Importar");
        botonImportar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                importarImagen();
            }
        });

        JButton botonCambiarCarpeta = new JButton("Cambiar carpeta");
        botonCambiarCarpeta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                cambiarCarpeta();
            }
        });

        JButton[] botonesBarra = {botonImportar, botonCambiarCarpeta};
        for (JButton boton : botonesBarra) {
            EstiloMinecraft.aplicarBoton(boton);
        }

        barra.add(botonImportar);
        barra.add(botonCambiarCarpeta);
        return barra;
    }

    public void mostrarArchivo(File archivo) {
        archivoObjetivoInicial = archivo;
        cargarCarpeta(archivo.getParentFile());
    }

    private void cambiarCarpeta() {
        JFileChooser selector = new JFileChooser(raizNavegable);
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int resultado = selector.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            cargarCarpeta(selector.getSelectedFile());
        }
    }

    private void importarImagen() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imagenes", "jpg", "jpeg", "png", "gif", "bmp"));
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File origen = selector.getSelectedFile();
        File destino = new File(carpetaActual, origen.getName());
        if (destino.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe una imagen llamada '" + origen.getName() + "' en esta carpeta.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            SistemaArchivos.copiarArchivo(origen, destino);
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(this, "Error al importar: " + excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        mostrarArchivo(destino);
    }

    private void cargarCarpeta(File carpeta) {
        carpetaActual = carpeta;
        final int idSolicitud = ++solicitudActual;
        etiquetaEstado.setText("Cargando imagenes...");
        botonAnterior.setEnabled(false);
        botonSiguiente.setEnabled(false);
        etiquetaImagen.setIcon(null);
        etiquetaImagen.setText("");
        HiloCargaImagenes hilo = new HiloCargaImagenes(carpeta, new HiloCargaImagenes.Callback() {
            public void alCargar(ListaEnlazada<File> imagenesCargadas) {
                if (idSolicitud == solicitudActual) {
                    aplicarImagenesCargadas(imagenesCargadas);
                }
            }
        });
        hilo.start();
    }

    private void aplicarImagenesCargadas(ListaEnlazada<File> imagenesCargadas) {
        imagenes = imagenesCargadas;
        indiceActual = 0;
        if (archivoObjetivoInicial != null) {
            for (int i = 0; i < imagenes.tamanio(); i++) {
                if (imagenes.obtener(i).equals(archivoObjetivoInicial)) {
                    indiceActual = i;
                    break;
                }
            }
            archivoObjetivoInicial = null;
        }
        construirTiraMiniaturas();
        if (imagenes.tamanio() == 0) {
            etiquetaEstado.setText("No hay imagenes en " + carpetaActual.getName());
            return;
        }
        botonAnterior.setEnabled(true);
        botonSiguiente.setEnabled(true);
        mostrarImagenActual();
    }

    private void construirTiraMiniaturas() {
        panelMiniaturas.removeAll();
        botonesMiniatura = new JButton[imagenes.tamanio()];
        for (int i = 0; i < imagenes.tamanio(); i++) {
            final int indice = i;
            File archivo = imagenes.obtener(i);
            JButton boton = new JButton(cargarMiniatura(archivo));
            boton.setToolTipText(archivo.getName());
            boton.setPreferredSize(new Dimension(TAMANIO_MINIATURA, TAMANIO_MINIATURA));
            boton.setMargin(new java.awt.Insets(1, 1, 1, 1));
            boton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evento) {
                    indiceActual = indice;
                    mostrarImagenActual();
                }
            });
            botonesMiniatura[i] = boton;
            panelMiniaturas.add(boton);
        }
        panelMiniaturas.revalidate();
        panelMiniaturas.repaint();
    }

    private ImageIcon cargarMiniatura(File archivo) {
        ImageIcon original = new ImageIcon(archivo.getPath());
        if (original.getIconWidth() <= 0) {
            return null;
        }
        Image escalada = original.getImage().getScaledInstance(TAMANIO_MINIATURA - 8, TAMANIO_MINIATURA - 8, Image.SCALE_FAST);
        return new ImageIcon(escalada);
    }

    private void actualizarResaltadoMiniaturas() {
        if (botonesMiniatura == null) {
            return;
        }
        for (int i = 0; i < botonesMiniatura.length; i++) {
            JButton boton = botonesMiniatura[i];
            if (boton == null) {
                continue;
            }
            if (i == indiceActual) {
                boton.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            } else {
                boton.setBorder(BorderFactory.createLineBorder(EstiloMinecraft.GRIS_OSCURO, 1));
            }
        }
        if (indiceActual >= 0 && indiceActual < botonesMiniatura.length && botonesMiniatura[indiceActual] != null) {
            Rectangle limites = botonesMiniatura[indiceActual].getBounds();
            panelMiniaturas.scrollRectToVisible(limites);
        }
    }

    private void mostrarImagenActual() {
        File archivo = imagenes.obtener(indiceActual);
        ImageIcon original = new ImageIcon(archivo.getPath());
        int anchoOriginal = original.getIconWidth();
        int altoOriginal = original.getIconHeight();
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            etiquetaImagen.setIcon(null);
            etiquetaImagen.setText("No se pudo cargar la imagen.");
            etiquetaEstado.setText((indiceActual + 1) + " de " + imagenes.tamanio() + " - " + archivo.getName());
            actualizarResaltadoMiniaturas();
            return;
        }
        double escala = Math.min((double) ANCHO_MAXIMO / anchoOriginal, (double) ALTO_MAXIMO / altoOriginal);
        int anchoFinal = (int) (anchoOriginal * escala);
        int altoFinal = (int) (altoOriginal * escala);
        Image escalada = original.getImage().getScaledInstance(anchoFinal, altoFinal, Image.SCALE_SMOOTH);
        etiquetaImagen.setText("");
        etiquetaImagen.setIcon(new ImageIcon(escalada));
        etiquetaEstado.setText((indiceActual + 1) + " de " + imagenes.tamanio() + " - " + archivo.getName());
        actualizarResaltadoMiniaturas();
    }

    private void mostrarAnterior() {
        if (imagenes == null || imagenes.tamanio() == 0) {
            return;
        }
        indiceActual = (indiceActual == 0) ? imagenes.tamanio() - 1 : indiceActual - 1;
        mostrarImagenActual();
    }

    private void mostrarSiguiente() {
        if (imagenes == null || imagenes.tamanio() == 0) {
            return;
        }
        indiceActual = (indiceActual + 1) % imagenes.tamanio();
        mostrarImagenActual();
    }

    private static class FlechaIcon implements Icon {

        private boolean izquierda;
        private int tamanio;

        FlechaIcon(boolean izquierda, int tamanio) {
            this.izquierda = izquierda;
            this.tamanio = tamanio;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            int mitad = tamanio / 2;
            Polygon triangulo = new Polygon();
            if (izquierda) {
                triangulo.addPoint(x + tamanio - 6, y + 4);
                triangulo.addPoint(x + tamanio - 6, y + tamanio - 4);
                triangulo.addPoint(x + 4, y + mitad);
            } else {
                triangulo.addPoint(x + 6, y + 4);
                triangulo.addPoint(x + 6, y + tamanio - 4);
                triangulo.addPoint(x + tamanio - 4, y + mitad);
            }
            g2.fillPolygon(triangulo);
            g2.dispose();
        }

        public int getIconWidth() {
            return tamanio;
        }

        public int getIconHeight() {
            return tamanio;
        }
    }
}
