package insta;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String autor;
    private LocalDateTime fecha;
    private String contenido;
    private String imagenRuta;
    private String stickerRuta;

    public Publicacion(String autor, String contenido, String imagenRuta, String stickerRuta) {
        this.autor = autor;
        this.contenido = contenido;
        this.imagenRuta = imagenRuta;
        this.stickerRuta = stickerRuta;
        this.fecha = LocalDateTime.now();
    }

    public String getAutor() {
        return autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }

    public String getStickerRuta() {
        return stickerRuta;
    }

    @Override
    public String toString() {
        return autor + " escribio:\n\"" + contenido + "\" - " + fecha;
    }
}