package miniwindows;

import miniwindows.estructuras.ListaEnlazada;
import miniwindows.archivos.ArchivoBinario;
import miniwindows.excepciones.ArchivoCorruptoException;
import miniwindows.excepciones.CredencialesInvalidasException;
import miniwindows.excepciones.UsuarioDuplicadoException;

import java.io.File;
import java.io.IOException;

public class GestorUsuarios {

    private ListaEnlazada<Usuario> usuarios;

    public GestorUsuarios() {
        cargar();
    }

    private void cargar() {
        String ruta = SistemaArchivos.obtenerRutaArchivoUsuarios();
        boolean puedeGuardar = true;
        if (ArchivoBinario.existeArchivo(ruta)) {
            try {
                Object leido = ArchivoBinario.leerObjeto(ruta);
                usuarios = (ListaEnlazada<Usuario>) leido;
                return;
            } catch (ArchivoCorruptoException e) {
                puedeGuardar = guardarRespaldoDanado(ruta);
            }
        }
        usuarios = new ListaEnlazada<Usuario>();
        sembrarAdministrador(puedeGuardar);
    }

    private boolean guardarRespaldoDanado(String ruta) {
        File respaldo = new File(ruta + "." + System.currentTimeMillis() + ".danado");
        if (new File(ruta).renameTo(respaldo)) {
            System.out.println("usuarios.sop no se pudo leer, se guardo una copia en " + respaldo.getName());
            return true;
        }
        System.out.println("usuarios.sop no se pudo leer ni respaldar, no se sobrescribe.");
        return false;
    }

    private void recargar() {
        String ruta = SistemaArchivos.obtenerRutaArchivoUsuarios();
        if (!ArchivoBinario.existeArchivo(ruta)) {
            return;
        }
        try {
            Object leido = ArchivoBinario.leerObjeto(ruta);
            usuarios = (ListaEnlazada<Usuario>) leido;
        } catch (ArchivoCorruptoException e) {
            System.out.println("No se pudo recargar usuarios.sop: " + e.getMessage());
        }
    }

    private void sembrarAdministrador(boolean guardarEnDisco) {
        Usuario admin = new Usuario("Administrador", "admin", "admin123", true);
        usuarios.agregar(admin);
        if (guardarEnDisco) {
            guardar();
        }
        SistemaArchivos.crearCarpetasBase("admin");
    }

    private void guardar() {
        try {
            ArchivoBinario.guardarObjeto(SistemaArchivos.obtenerRutaArchivoUsuarios(), usuarios);
        } catch (IOException e) {
            System.out.println("Error al guardar usuarios.sop: " + e.getMessage());
        }
    }

    private Usuario buscarUsuario(String nombreUsuario) {
        for (int i = 0; i < usuarios.tamanio(); i++) {
            Usuario u = usuarios.obtener(i);
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                return u;
            }
        }
        return null;
    }

    public void crearUsuario(String nombreCompleto, String nombreUsuario, String password, boolean administrador) throws UsuarioDuplicadoException {
        recargar();
        if (buscarUsuario(nombreUsuario) != null) {
            throw new UsuarioDuplicadoException("El nombre de usuario '" + nombreUsuario + "' ya esta en uso.");
        }
        Usuario nuevo = new Usuario(nombreCompleto, nombreUsuario, password, administrador);
        usuarios.agregar(nuevo);
        guardar();
        SistemaArchivos.crearCarpetasBase(nombreUsuario);
    }

    public Usuario validarCredenciales(String nombreUsuario, String password) throws CredencialesInvalidasException {
        recargar();
        Usuario usuario = buscarUsuario(nombreUsuario);
        if (usuario == null || !usuario.getPassword().equals(password)) {
            throw new CredencialesInvalidasException("Usuario o contrasenia incorrectos.");
        }
        return usuario;
    }

    public ListaEnlazada<Usuario> listarUsuarios() {
        recargar();
        return usuarios;
    }
}
