package insta;

import java.io.Serializable;

public class Respuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean exito;
    private final String mensaje;
    private final Object datos;

    public Respuesta(boolean exito, String mensaje, Object datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public static Respuesta ok(Object datos) {
        return new Respuesta(true, "OK", datos);
    }

    public static Respuesta error(String mensaje) {
        return new Respuesta(false, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Object getDatos() {
        return datos;
    }
}