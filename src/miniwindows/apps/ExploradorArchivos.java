package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.hilos.HiloOrganizador;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;

public class ExploradorArchivos extends JFrame {

    private Usuario usuarioActual;
    private File raizNavegable;
    private JTree arbol;
    private DefaultTreeModel modelo;
    private JComboBox<String> comboOrden;
    private JButton botonOrganizar;
    private File archivoCopiado;

    public ExploradorArchivos(Usuario usuarioActual) {
        super("Explorador de archivos - " + usuarioActual.getNombreUsuario());
        this.usuarioActual = usuarioActual;
        raizNavegable = usuarioActual.isAdministrador()
                ? SistemaArchivos.obtenerRaiz()
                : SistemaArchivos.obtenerCarpetaUsuario(usuarioActual.getNombreUsuario());
        armarVentana();
    }

    private void armarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        EstiloMinecraft.aplicarVentana(this);

        NodoArbol raiz = new NodoArbol(raizNavegable);
        construirArbol(raiz, raizNavegable, SistemaArchivos.comparadorPorNombre());
        modelo = new DefaultTreeModel(raiz);
        arbol = new JTree(modelo);
        arbol.setBackground(EstiloMinecraft.GRIS_FONDO);
        arbol.expandRow(0);
        arbol.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    abrirArchivoSeleccionado();
                }
            }
        });
        JScrollPane scrollArbol = new JScrollPane(arbol);
        EstiloMinecraft.aplicarRanura(scrollArbol);
        add(scrollArbol, BorderLayout.CENTER);

        add(armarBarraHerramientas(), BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evento) {
                actualizarArbol();
            }
        });
    }

    private JPanel armarBarraHerramientas() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        EstiloMinecraft.aplicarPanel(contenedor);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        EstiloMinecraft.aplicarPanel(barra);

        JButton botonNuevaCarpeta = new JButton("Nueva carpeta");
        botonNuevaCarpeta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                crearCarpeta();
            }
        });

        JButton botonNuevoArchivo = new JButton("Nuevo archivo");
        botonNuevoArchivo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                crearArchivo();
            }
        });

        JButton botonRenombrar = new JButton("Renombrar");
        botonRenombrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                renombrar();
            }
        });

        JButton botonCopiar = new JButton("Copiar");
        botonCopiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                copiar();
            }
        });

        JButton botonPegar = new JButton("Pegar");
        botonPegar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                pegar();
            }
        });

        JButton botonEliminar = new JButton("Eliminar");
        botonEliminar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                eliminar();
            }
        });

        botonOrganizar = new JButton("Organizar");
        botonOrganizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                organizar();
            }
        });

        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                actualizarArbol();
            }
        });

        comboOrden = new JComboBox<String>(new String[]{"Nombre", "Fecha", "Tipo", "Tamaño"});
        comboOrden.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                actualizarArbol();
            }
        });

        JButton[] botonesBarra = {botonNuevaCarpeta, botonNuevoArchivo, botonRenombrar, botonCopiar,
                botonPegar, botonEliminar, botonOrganizar, botonActualizar};
        for (JButton boton : botonesBarra) {
            EstiloMinecraft.aplicarBoton(boton);
        }

        barra.add(botonNuevaCarpeta);
        barra.add(botonNuevoArchivo);
        barra.add(botonRenombrar);
        barra.add(botonCopiar);
        barra.add(botonPegar);
        barra.add(botonEliminar);
        barra.add(botonOrganizar);
        barra.add(botonActualizar);

        JPanel filaOrden = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        EstiloMinecraft.aplicarPanel(filaOrden);
        JLabel etiquetaOrden = new JLabel("Ordenar por:");
        filaOrden.add(etiquetaOrden);
        filaOrden.add(comboOrden);

        contenedor.add(barra);
        contenedor.add(filaOrden);
        return contenedor;
    }

    private void construirArbol(NodoArbol nodoPadre, File carpeta, Comparator<File> comparador) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return;
        }
        ListaEnlazada<File> ordenados = new ListaEnlazada<File>();
        for (File hijo : hijos) {
            ordenados.insertarOrdenado(hijo, comparador);
        }
        for (int i = 0; i < ordenados.tamanio(); i++) {
            File hijo = ordenados.obtener(i);
            NodoArbol nodoHijo = new NodoArbol(hijo);
            nodoPadre.add(nodoHijo);
            if (hijo.isDirectory()) {
                construirArbol(nodoHijo, hijo, comparador);
            }
        }
    }

    private void actualizarArbol() {
        actualizarArbol(null);
    }

    private void actualizarArbol(File archivoASeleccionar) {
        ListaEnlazada<File> expandidas = obtenerCarpetasExpandidas();
        File seleccion = archivoASeleccionar != null ? archivoASeleccionar : obtenerArchivoSeleccionado();
        NodoArbol nuevaRaiz = new NodoArbol(raizNavegable);
        construirArbol(nuevaRaiz, raizNavegable, obtenerComparadorSeleccionado());
        modelo.setRoot(nuevaRaiz);
        arbol.expandRow(0);
        restaurarEstado(nuevaRaiz, expandidas, seleccion);
    }

    private ListaEnlazada<File> obtenerCarpetasExpandidas() {
        ListaEnlazada<File> expandidas = new ListaEnlazada<File>();
        NodoArbol raizActual = (NodoArbol) modelo.getRoot();
        Enumeration<TreePath> caminos = arbol.getExpandedDescendants(new TreePath(raizActual));
        if (caminos == null) {
            return expandidas;
        }
        while (caminos.hasMoreElements()) {
            NodoArbol nodo = (NodoArbol) caminos.nextElement().getLastPathComponent();
            expandidas.agregar((File) nodo.getUserObject());
        }
        return expandidas;
    }

    private void restaurarEstado(NodoArbol nodo, ListaEnlazada<File> expandidas, File seleccion) {
        File archivo = (File) nodo.getUserObject();
        TreePath camino = new TreePath(nodo.getPath());
        if (expandidas.contiene(archivo)) {
            arbol.expandPath(camino);
        }
        if (archivo.equals(seleccion)) {
            arbol.setSelectionPath(camino);
            arbol.scrollPathToVisible(camino);
        }
        for (int i = 0; i < nodo.getChildCount(); i++) {
            restaurarEstado((NodoArbol) nodo.getChildAt(i), expandidas, seleccion);
        }
    }

    private Comparator<File> obtenerComparadorSeleccionado() {
        String criterio = (String) comboOrden.getSelectedItem();
        if ("Fecha".equals(criterio)) {
            return SistemaArchivos.comparadorPorFecha();
        }
        if ("Tipo".equals(criterio)) {
            return SistemaArchivos.comparadorPorTipo();
        }
        if ("Tamaño".equals(criterio)) {
            return SistemaArchivos.comparadorPorTamano();
        }
        return SistemaArchivos.comparadorPorNombre();
    }

    private File obtenerArchivoSeleccionado() {
        NodoArbol seleccionado = (NodoArbol) arbol.getLastSelectedPathComponent();
        if (seleccionado == null) {
            return null;
        }
        return (File) seleccionado.getUserObject();
    }

    private void abrirArchivoSeleccionado() {
        File archivo = obtenerArchivoSeleccionado();
        if (archivo == null || archivo.isDirectory()) {
            return;
        }
        String nombre = archivo.getName().toLowerCase();
        if (nombre.endsWith(".txt")) {
            EditorTexto editor = new EditorTexto(usuarioActual);
            editor.abrirArchivo(archivo);
            editor.setVisible(true);
        } else if (esImagen(nombre)) {
            VisorImagenes visor = new VisorImagenes(usuarioActual);
            visor.mostrarArchivo(archivo);
            visor.setVisible(true);
        } else if (nombre.endsWith(".mp3")) {
            Reproductor reproductor = new Reproductor(usuarioActual);
            reproductor.reproducirArchivoEspecifico(archivo);
            reproductor.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No hay una aplicacion asociada para abrir '" + archivo.getName() + "'.",
                    "Sin aplicacion asociada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean esImagen(String nombreMinuscula) {
        return nombreMinuscula.endsWith(".jpg") || nombreMinuscula.endsWith(".jpeg")
                || nombreMinuscula.endsWith(".png") || nombreMinuscula.endsWith(".gif")
                || nombreMinuscula.endsWith(".bmp");
    }

    private File obtenerCarpetaDestino() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null) {
            return raizNavegable;
        }
        if (seleccionado.isDirectory()) {
            return seleccionado;
        }
        return seleccionado.getParentFile();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String mensajeProtegido(File archivo) {
        return "'" + archivo.getName() + "' es del sistema o es la carpeta de un usuario, no se puede modificar.";
    }

    private void crearCarpeta() {
        File carpetaDestino = obtenerCarpetaDestino();
        if (SistemaArchivos.esArchivoDelSistema(carpetaDestino)) {
            mostrarError("No se puede crear nada dentro de '" + carpetaDestino.getName() + "' porque es del sistema.");
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (nombre == null) {
            return;
        }
        nombre = nombre.trim();
        if (!SistemaArchivos.nombreValido(nombre)) {
            mostrarError("Nombre invalido.");
            return;
        }
        File nueva = new File(carpetaDestino, nombre);
        if (nueva.exists()) {
            mostrarError("Ya existe '" + nombre + "'.");
            return;
        }
        if (!nueva.mkdirs()) {
            mostrarError("No se pudo crear la carpeta '" + nombre + "'.");
            return;
        }
        actualizarArbol(nueva);
    }

    private void crearArchivo() {
        File carpetaDestino = obtenerCarpetaDestino();
        if (SistemaArchivos.esArchivoDelSistema(carpetaDestino)) {
            mostrarError("No se puede crear nada dentro de '" + carpetaDestino.getName() + "' porque es del sistema.");
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo archivo:");
        if (nombre == null) {
            return;
        }
        nombre = nombre.trim();
        if (!SistemaArchivos.nombreValido(nombre)) {
            mostrarError("Nombre invalido.");
            return;
        }
        File nuevo = new File(carpetaDestino, nombre);
        if (nuevo.exists()) {
            mostrarError("Ya existe '" + nombre + "'.");
            return;
        }
        try {
            if (!nuevo.createNewFile()) {
                mostrarError("No se pudo crear el archivo '" + nombre + "'.");
                return;
            }
        } catch (IOException excepcion) {
            mostrarError("Error al crear el archivo: " + excepcion.getMessage());
            return;
        }
        actualizarArbol(nuevo);
    }

    private void renombrar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            mostrarError("Selecciona un archivo o carpeta para renombrar.");
            return;
        }
        if (SistemaArchivos.esProtegido(seleccionado)) {
            mostrarError(mensajeProtegido(seleccionado));
            return;
        }
        String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", seleccionado.getName());
        if (nuevoNombre == null) {
            return;
        }
        nuevoNombre = nuevoNombre.trim();
        if (!SistemaArchivos.nombreValido(nuevoNombre)) {
            mostrarError("Nombre invalido.");
            return;
        }
        File destino = new File(seleccionado.getParentFile(), nuevoNombre);
        boolean soloCambiaMayusculas = nuevoNombre.equalsIgnoreCase(seleccionado.getName());
        if (destino.exists() && !soloCambiaMayusculas) {
            mostrarError("Ya existe '" + nuevoNombre + "'.");
            return;
        }
        if (!seleccionado.renameTo(destino)) {
            mostrarError("No se pudo renombrar '" + seleccionado.getName() + "'. Puede estar abierto en otra aplicacion.");
            return;
        }
        if (seleccionado.equals(archivoCopiado)) {
            archivoCopiado = destino;
        }
        actualizarArbol(destino);
    }

    private void copiar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            mostrarError("Selecciona un archivo o carpeta para copiar.");
            return;
        }
        archivoCopiado = seleccionado;
        JOptionPane.showMessageDialog(this, "'" + seleccionado.getName() + "' copiado. Selecciona el destino y presiona Pegar.");
    }

    private void pegar() {
        if (archivoCopiado == null) {
            mostrarError("No hay nada copiado.");
            return;
        }
        if (!archivoCopiado.exists()) {
            mostrarError("'" + archivoCopiado.getName() + "' ya no existe.");
            archivoCopiado = null;
            return;
        }
        File carpetaDestino = obtenerCarpetaDestino();
        if (SistemaArchivos.esArchivoDelSistema(carpetaDestino)) {
            mostrarError("No se puede pegar dentro de '" + carpetaDestino.getName() + "' porque es del sistema.");
            return;
        }
        if (archivoCopiado.isDirectory() && SistemaArchivos.estaDentroDe(carpetaDestino, archivoCopiado)) {
            mostrarError("No se puede pegar '" + archivoCopiado.getName() + "' dentro de si misma.");
            return;
        }
        File destino = SistemaArchivos.nombreDeCopiaDisponible(carpetaDestino, archivoCopiado);
        try {
            if (archivoCopiado.isDirectory()) {
                SistemaArchivos.copiarCarpetaRecursivo(archivoCopiado, destino);
            } else {
                SistemaArchivos.copiarArchivo(archivoCopiado, destino);
            }
        } catch (IOException excepcion) {
            mostrarError("Error al pegar: " + excepcion.getMessage());
            return;
        }
        actualizarArbol(destino);
    }

    private void eliminar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            mostrarError("Selecciona un archivo o carpeta para eliminar.");
            return;
        }
        if (SistemaArchivos.esProtegido(seleccionado)) {
            mostrarError(mensajeProtegido(seleccionado));
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar '" + seleccionado.getName() + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        boolean eliminado = SistemaArchivos.eliminarRecursivo(seleccionado);
        if (archivoCopiado != null && SistemaArchivos.estaDentroDe(archivoCopiado, seleccionado)) {
            archivoCopiado = null;
        }
        actualizarArbol(seleccionado.getParentFile());
        if (!eliminado) {
            mostrarError("No se pudo eliminar todo '" + seleccionado.getName() + "'. Puede haber archivos abiertos en otra aplicacion.");
        }
    }

    private void organizar() {
        File carpeta = obtenerCarpetaDestino();
        if (SistemaArchivos.esArchivoDelSistema(carpeta)) {
            mostrarError("No se puede organizar '" + carpeta.getName() + "' porque es del sistema.");
            return;
        }
        botonOrganizar.setEnabled(false);
        HiloOrganizador hilo = new HiloOrganizador(carpeta, new Runnable() {
            public void run() {
                botonOrganizar.setEnabled(true);
                actualizarArbol();
                JOptionPane.showMessageDialog(ExploradorArchivos.this, "Organizacion completada.");
            }
        });
        hilo.start();
    }

    private static class NodoArbol extends DefaultMutableTreeNode {

        NodoArbol(File archivo) {
            super(archivo);
        }

        public String toString() {
            File archivo = (File) getUserObject();
            String nombre = archivo.getName();
            return nombre.isEmpty() ? archivo.getPath() : nombre;
        }
    }
}
