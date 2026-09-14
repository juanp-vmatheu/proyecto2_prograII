package insta;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    private String emisor;
    private String receptor;
    private LocalDateTime fechaHora;
    private String contenido;
    private TipoMensaje tipo;
    private boolean leido;

    public Mensaje(String emisor, String receptor, String contenido, TipoMensaje tipo) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
        this.leido = false;
    }

    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getContenido() {
        return contenido;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public boolean isLeido() {
        return leido;
    }

    public void marcarLeido() {
        this.leido = true;
    }
}