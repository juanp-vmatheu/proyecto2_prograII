package miniwindows;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String nombreCompleto;
    private String nombreUsuario;
    private String password;
    private boolean administrador;

    public Usuario(String nombreCompleto, String nombreUsuario, String password, boolean administrador) {
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.administrador = administrador;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdministrador() {
        return administrador;
    }

    public String toString() {
        return nombreUsuario + (administrador ? " (administrador)" : "");
    }
}
