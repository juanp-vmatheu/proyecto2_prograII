package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.Usuario;
import miniwindows.estructuras.ListaEnlazada;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class ExploradorArchivos extends JFrame {

    private File raizNavegable;
    private JTree arbol;
    private DefaultTreeModel modelo;
    private File archivoCopiado;

    public ExploradorArchivos(Usuario usuarioActual) {
        super("Explorador de archivos - " + usuarioActual.getNombreUsuario());
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

        NodoArbol raiz = new NodoArbol(raizNavegable);
        construirArbol(raiz, raizNavegable, SistemaArchivos.comparadorPorNombre());
        modelo = new DefaultTreeModel(raiz);
        arbol = new JTree(modelo);
        arbol.expandRow(0);
        add(new JScrollPane(arbol), BorderLayout.CENTER);

        add(armarBarraHerramientas(), BorderLayout.NORTH);
    }

    private JPanel armarBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

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

        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                actualizarArbol();
            }
        });

        barra.add(botonNuevaCarpeta);
        barra.add(botonNuevoArchivo);
        barra.add(botonRenombrar);
        barra.add(botonCopiar);
        barra.add(botonPegar);
        barra.add(botonEliminar);
        barra.add(botonActualizar);
        return barra;
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
        NodoArbol nuevaRaiz = new NodoArbol(raizNavegable);
        construirArbol(nuevaRaiz, raizNavegable, SistemaArchivos.comparadorPorNombre());
        modelo.setRoot(nuevaRaiz);
        arbol.expandRow(0);
    }

    private File obtenerArchivoSeleccionado() {
        NodoArbol seleccionado = (NodoArbol) arbol.getLastSelectedPathComponent();
        if (seleccionado == null) {
            return null;
        }
        return (File) seleccionado.getUserObject();
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

    private void crearCarpeta() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (nombre == null || !SistemaArchivos.nombreValido(nombre.trim())) {
            if (nombre != null) {
                JOptionPane.showMessageDialog(this, "Nombre invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        File nueva = new File(obtenerCarpetaDestino(), nombre.trim());
        if (nueva.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe '" + nombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nueva.mkdirs();
        actualizarArbol();
    }

    private void crearArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo archivo:");
        if (nombre == null || !SistemaArchivos.nombreValido(nombre.trim())) {
            if (nombre != null) {
                JOptionPane.showMessageDialog(this, "Nombre invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        File nuevo = new File(obtenerCarpetaDestino(), nombre.trim());
        if (nuevo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe '" + nombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            nuevo.createNewFile();
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(this, "Error al crear el archivo: " + excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarArbol();
    }

    private void renombrar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta para renombrar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", seleccionado.getName());
        if (nuevoNombre == null || !SistemaArchivos.nombreValido(nuevoNombre.trim())) {
            if (nuevoNombre != null) {
                JOptionPane.showMessageDialog(this, "Nombre invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        File destino = new File(seleccionado.getParentFile(), nuevoNombre.trim());
        if (destino.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe '" + nuevoNombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        seleccionado.renameTo(destino);
        actualizarArbol();
    }

    private void copiar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta para copiar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        archivoCopiado = seleccionado;
        JOptionPane.showMessageDialog(this, "'" + seleccionado.getName() + "' copiado. Selecciona el destino y presiona Pegar.");
    }

    private void pegar() {
        if (archivoCopiado == null) {
            JOptionPane.showMessageDialog(this, "No hay nada copiado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File carpetaDestino = obtenerCarpetaDestino();
        File destino = new File(carpetaDestino, archivoCopiado.getName());
        if (destino.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe '" + archivoCopiado.getName() + "' en el destino.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (archivoCopiado.isDirectory()) {
                SistemaArchivos.copiarCarpetaRecursivo(archivoCopiado, destino);
            } else {
                SistemaArchivos.copiarArchivo(archivoCopiado, destino);
            }
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(this, "Error al pegar: " + excepcion.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarArbol();
    }

    private void eliminar() {
        File seleccionado = obtenerArchivoSeleccionado();
        if (seleccionado == null || seleccionado.equals(raizNavegable)) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar '" + seleccionado.getName() + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        SistemaArchivos.eliminarRecursivo(seleccionado);
        if (seleccionado.equals(archivoCopiado)) {
            archivoCopiado = null;
        }
        actualizarArbol();
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
