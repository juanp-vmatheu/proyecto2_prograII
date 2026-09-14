package insta;

import java.io.Serializable;

public class Sticker implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String ruta;
    private boolean personal;

    public Sticker(String nombre, String ruta, boolean personal) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.personal = personal;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRuta() {
        return ruta;
    }

    public boolean isPersonal() {
        return personal;
    }

    @Override
    public String toString() {
        return nombre;
    }
}