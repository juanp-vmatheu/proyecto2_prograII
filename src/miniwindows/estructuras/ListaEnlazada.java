package miniwindows.estructuras;

import java.io.Serializable;
import java.util.Comparator;

public class ListaEnlazada<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Nodo<T> primero;
    private int tamanio;

    public ListaEnlazada() {
        primero = null;
        tamanio = 0;
    }

    public boolean estaVacia() {
        return primero == null;
    }

    public int tamanio() {
        return tamanio;
    }

    public Nodo<T> getPrimero() {
        return primero;
    }

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<T>(dato);
        if (primero == null) {
            primero = nuevo;
        } else {
            Nodo<T> actual = primero;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamanio++;
    }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<T>(dato);
        nuevo.setSiguiente(primero);
        primero = nuevo;
        tamanio++;
    }

    public void insertarEnPosicion(int posicion, T dato) {
        if (posicion <= 0) {
            agregarAlInicio(dato);
            return;
        }
        if (posicion >= tamanio) {
            agregar(dato);
            return;
        }
        Nodo<T> anterior = primero;
        int contador = 0;
        while (contador < posicion - 1) {
            anterior = anterior.getSiguiente();
            contador++;
        }
        Nodo<T> nuevo = new Nodo<T>(dato);
        nuevo.setSiguiente(anterior.getSiguiente());
        anterior.setSiguiente(nuevo);
        tamanio++;
    }

    public void insertarOrdenado(T dato, Comparator<T> comparador) {
        Nodo<T> nuevo = new Nodo<T>(dato);
        if (primero == null || comparador.compare(dato, primero.getDato()) < 0) {
            nuevo.setSiguiente(primero);
            primero = nuevo;
            tamanio++;
            return;
        }
        Nodo<T> actual = primero;
        while (actual.getSiguiente() != null && comparador.compare(dato, actual.getSiguiente().getDato()) >= 0) {
            actual = actual.getSiguiente();
        }
        nuevo.setSiguiente(actual.getSiguiente());
        actual.setSiguiente(nuevo);
        tamanio++;
    }

    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            return null;
        }
        Nodo<T> actual = primero;
        int contador = 0;
        while (contador < posicion) {
            actual = actual.getSiguiente();
            contador++;
        }
        return actual.getDato();
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = primero;
        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public boolean eliminar(T dato) {
        if (primero == null) {
            return false;
        }
        if (primero.getDato().equals(dato)) {
            primero = primero.getSiguiente();
            tamanio--;
            return true;
        }
        Nodo<T> anterior = primero;
        while (anterior.getSiguiente() != null) {
            if (anterior.getSiguiente().getDato().equals(dato)) {
                anterior.setSiguiente(anterior.getSiguiente().getSiguiente());
                tamanio--;
                return true;
            }
            anterior = anterior.getSiguiente();
        }
        return false;
    }

    public T eliminarEnPosicion(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            return null;
        }
        if (posicion == 0) {
            T dato = primero.getDato();
            primero = primero.getSiguiente();
            tamanio--;
            return dato;
        }
        Nodo<T> anterior = primero;
        int contador = 0;
        while (contador < posicion - 1) {
            anterior = anterior.getSiguiente();
            contador++;
        }
        T dato = anterior.getSiguiente().getDato();
        anterior.setSiguiente(anterior.getSiguiente().getSiguiente());
        tamanio--;
        return dato;
    }

    public void vaciar() {
        primero = null;
        tamanio = 0;
    }

    public String toString() {
        String texto = "";
        Nodo<T> actual = primero;
        while (actual != null) {
            texto = texto + actual.getDato();
            if (actual.getSiguiente() != null) {
                texto = texto + ", ";
            }
            actual = actual.getSiguiente();
        }
        return texto;
    }
}
