package miniwindows.archivos;

import java.io.Serializable;

public class DocumentoTexto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String texto;
    private String nombreFuente;
    private int tamanioFuente;
    private int colorRGB;

    public DocumentoTexto(String texto, String nombreFuente, int tamanioFuente, int colorRGB) {
        this.texto = texto;
        this.nombreFuente = nombreFuente;
        this.tamanioFuente = tamanioFuente;
        this.colorRGB = colorRGB;
    }

    public String getTexto() {
        return texto;
    }

    public String getNombreFuente() {
        return nombreFuente;
    }

    public int getTamanioFuente() {
        return tamanioFuente;
    }

    public int getColorRGB() {
        return colorRGB;
    }
}
