package miniwindows.archivos;

import java.io.Serializable;

public class Cancion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreArchivo;
    private String titulo;
    private String artista;
    private String descripcion;
    private String rutaCaratula;

    public Cancion(String nombreArchivo, String titulo, String artista, String descripcion, String rutaCaratula) {
        this.nombreArchivo = nombreArchivo;
        this.titulo = titulo;
        this.artista = artista;
        this.descripcion = descripcion;
        this.rutaCaratula = rutaCaratula;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getArtista() {
        return artista;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getRutaCaratula() {
        return rutaCaratula;
    }
}
