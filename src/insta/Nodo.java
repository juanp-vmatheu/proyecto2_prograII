package insta;

import java.io.Serializable;

public class Nodo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    T dato;
    Nodo<T> siguiente;

    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}