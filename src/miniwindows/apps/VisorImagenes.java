package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.excepciones.CarpetaNoEncontradaException;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private String firmaCargada;

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

        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evento) {
                recargarSiCambio();
            }
        });
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

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void cambiarCarpeta() {
        JFileChooser selector = new JFileChooser(raizNavegable);
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File elegida = selector.getSelectedFile();
        if (!SistemaArchivos.estaDentroDe(elegida, raizNavegable)) {
            mostrarError("Solo puedes abrir carpetas dentro de tu espacio en Mini-Windows.");
            return;
        }
        cargarCarpeta(elegida);
    }

    private void importarImagen() {
        if (carpetaActual == null || !carpetaActual.isDirectory()) {
            mostrarError("La carpeta actual no existe, elige otra con 'Cambiar carpeta'.");
            return;
        }
        if (SistemaArchivos.esArchivoDelSistema(carpetaActual)) {
            mostrarError("No se pueden importar imagenes dentro de una carpeta del sistema.");
            return;
        }
        JFileChooser selector = new JFileChooser(raizNavegable);
        selector.setFileFilter(new FileNameExtensionFilter("Imagenes", "jpg", "jpeg", "png", "gif", "bmp"));
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File origen = selector.getSelectedFile();
        File destino = new File(carpetaActual, origen.getName());
        if (destino.exists()) {
            mostrarError("Ya existe una imagen llamada '" + origen.getName() + "' en esta carpeta.");
            return;
        }
        try {
            SistemaArchivos.copiarArchivo(origen, destino);
        } catch (IOException excepcion) {
            mostrarError("Error al importar: " + excepcion.getMessage());
            return;
        }
        mostrarArchivo(destino);
    }

    private void recargarSiCambio() {
        if (carpetaActual == null || SistemaArchivos.firmaArchivos(carpetaActual).equals(firmaCargada)) {
            return;
        }
        if (imagenes != null && indiceActual >= 0 && indiceActual < imagenes.tamanio()) {
            archivoObjetivoInicial = imagenes.obtener(indiceActual);
        }
        cargarCarpeta(carpetaActual);
    }

    private void cargarCarpeta(File carpeta) {
        carpetaActual = carpeta;
        final int idSolicitud = ++solicitudActual;
        botonAnterior.setEnabled(false);
        botonSiguiente.setEnabled(false);
        etiquetaImagen.setIcon(null);
        etiquetaImagen.setText("");
        try {
            SistemaArchivos.verificarCarpeta(carpeta);
        } catch (CarpetaNoEncontradaException excepcion) {
            firmaCargada = "";
            imagenes = new ListaEnlazada<File>();
            construirTiraMiniaturas(new ListaEnlazada<ImageIcon>());
            etiquetaEstado.setText(excepcion.getMessage());
            return;
        }
        firmaCargada = SistemaArchivos.firmaArchivos(carpeta);
        etiquetaEstado.setText("Cargando imagenes...");
        HiloCargaImagenes hilo = new HiloCargaImagenes(carpeta, TAMANIO_MINIATURA - 8, new HiloCargaImagenes.Callback() {
            public void alCargar(ListaEnlazada<File> imagenesCargadas, ListaEnlazada<ImageIcon> miniaturas) {
                if (idSolicitud == solicitudActual) {
                    aplicarImagenesCargadas(imagenesCargadas, miniaturas);
                }
            }
        });
        hilo.start();
    }

    private void aplicarImagenesCargadas(ListaEnlazada<File> imagenesCargadas, ListaEnlazada<ImageIcon> miniaturas) {
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
        construirTiraMiniaturas(miniaturas);
        if (imagenes.tamanio() == 0) {
            etiquetaEstado.setText("No hay imagenes en " + carpetaActual.getName());
            return;
        }
        botonAnterior.setEnabled(true);
        botonSiguiente.setEnabled(true);
        mostrarImagenActual();
    }

    private void construirTiraMiniaturas(ListaEnlazada<ImageIcon> miniaturas) {
        panelMiniaturas.removeAll();
        botonesMiniatura = new JButton[imagenes.tamanio()];
        for (int i = 0; i < imagenes.tamanio(); i++) {
            final int indice = i;
            File archivo = imagenes.obtener(i);
            ImageIcon miniatura = miniaturas.obtener(i);
            JButton boton = new JButton(miniatura);
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
        ImageIcon original = HiloCargaImagenes.cargarSinCache(archivo);
        etiquetaEstado.setText((indiceActual + 1) + " de " + imagenes.tamanio() + " - " + archivo.getName());
        if (original == null) {
            etiquetaImagen.setIcon(null);
            etiquetaImagen.setText("No se pudo cargar la imagen.");
            actualizarResaltadoMiniaturas();
            return;
        }
        int anchoOriginal = original.getIconWidth();
        int altoOriginal = original.getIconHeight();
        double escala = Math.min((double) ANCHO_MAXIMO / anchoOriginal, (double) ALTO_MAXIMO / altoOriginal);
        int anchoFinal = (int) (anchoOriginal * escala);
        int altoFinal = (int) (altoOriginal * escala);
        Image escalada = original.getImage().getScaledInstance(anchoFinal, altoFinal, Image.SCALE_SMOOTH);
        etiquetaImagen.setText("");
        etiquetaImagen.setIcon(new ImageIcon(escalada));
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
