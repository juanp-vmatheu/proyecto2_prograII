package miniwindows.apps;

import miniwindows.SistemaArchivos;
import miniwindows.archivos.ArchivoBinario;
import miniwindows.archivos.DocumentoTexto;
import miniwindows.estructuras.ListaEnlazada;
import miniwindows.excepciones.ArchivoCorruptoException;
import miniwindows.excepciones.CarpetaNoEncontradaException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Comandos_logica {

    public static final String SALIR = " __EXIT__";
    public static final String LIMPIAR = " __CLS__";

    private File raiz;
    private File actual;

    private boolean modoEscritura = false;
    private boolean modoAgregar = false;
    private File archivoEscritura = null;
    private List<String> lineasEscritura = null;

    public Comandos_logica(File raizUsuario) {
        raiz = raizUsuario;
        actual = raiz;
    }

    public String prompt() {
        if (modoEscritura) {
            return "";
        }
        return actual.getAbsolutePath();
    }

    private boolean nombreValido(String nombre) {
        return SistemaArchivos.nombreValido(nombre);
    }

    private File devolverRuta(String nombre) {
        return new File(actual, nombre);
    }

    public String ejecutar(String txtIngresado) {
        if (modoEscritura) {
            return escribirLinea(txtIngresado);
        }
        String texto = txtIngresado.trim();
        if (texto.isEmpty()) {
            return "";
        }
        String aviso = verificarCarpetaActual();
        String comando;
        String resto;
        int espacio = texto.indexOf(' ');
        if (espacio == -1) {
            comando = texto;
            resto = "";
        } else {
            comando = texto.substring(0, espacio);
            resto = texto.substring(espacio + 1).trim();
        }
        String resultado = ejecutarComando(comando.toLowerCase(), resto);
        if (aviso.isEmpty() || resultado.equals(SALIR) || resultado.equals(LIMPIAR)) {
            return resultado;
        }
        return resultado.isEmpty() ? aviso : aviso + "\n" + resultado;
    }

    private String verificarCarpetaActual() {
        if (actual.isDirectory()) {
            return "";
        }
        while (!actual.isDirectory() && !actual.equals(raiz)) {
            actual = actual.getParentFile();
        }
        if (!actual.isDirectory()) {
            raiz.mkdirs();
            actual = raiz;
        }
        return "Aviso: la carpeta en la que estabas ya no existe, ahora estas en '" + actual.getName() + "'.";
    }

    private String ejecutarComando(String comando, String resto) {
        switch (comando) {
            case "mkdir":
                return Mkdir(resto);
            case "mfile":
                return Mfile(resto);
            case "..":
                return subirPadre();
            case "cd..":
                return subirPadre();
            case "cd":
                return Cd(resto);
            case "rm":
                return Rm(resto);
            case "dir":
                return Dir();
            case "date":
                return Date();
            case "time":
                return Time();
            case "wr":
                return Wr(resto);
            case "rd":
                return Rd(resto);
            case "ap":
                return Ap(resto);
            case "ren":
                return Ren(resto);
            case "copy":
                return Copy(resto);
            case "find":
                return Find(resto);
            case "info":
                return Info(resto);
            case "tree":
                return Tree();
            case "cls":
                return LIMPIAR;
            case "help":
                return Help();
            case "exit":
                return SALIR;
            case "grep":
                return Grep(resto);
            default:
                return "'" + comando + "' no se reconoce como un comando interno o externo.";
        }
    }

    private String errorProtegido(String nombre) {
        return "Error: '" + nombre + "' esta protegido por el sistema y no se puede modificar.";
    }

    private String Mkdir(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File carpeta = devolverRuta(nombre);
        if (SistemaArchivos.esArchivoDelSistema(carpeta)) {
            return errorProtegido(nombre);
        }
        if (carpeta.exists()) {
            return "Error: ya existe una carpeta con este nombre";
        }
        boolean fueCreada = carpeta.mkdir();
        return fueCreada ? "Carpeta '" + nombre + "' creada correctamente."
                : "Error: no se pudo crear la carpeta '" + nombre + "'.";
    }

    private String Mfile(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo = devolverRuta(nombre);
        if (SistemaArchivos.esArchivoDelSistema(archivo)) {
            return errorProtegido(nombre);
        }
        if (archivo.exists()) {
            return "Error: ya existe un archivo con este nombre";
        }
        try {
            boolean fueCreado = archivo.createNewFile();
            return fueCreado ? "Archivo '" + nombre + "' creado correctamente."
                    : "Error: no se pudo crear el archivo '" + nombre + "'.";
        } catch (IOException e) {
            return "Error al crear el archivo: " + e.getMessage();
        }
    }

    private String subirPadre() {
        if (actual.equals(raiz)) {
            return "";
        }
        actual = actual.getParentFile();
        return "";
    }

    private String Cd(String nombre) {
        if (nombre.equals("..")) {
            return subirPadre();
        }
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File destino = devolverRuta(nombre);
        try {
            SistemaArchivos.verificarCarpeta(destino);
        } catch (CarpetaNoEncontradaException excepcion) {
            return "Error: " + excepcion.getMessage();
        }
        actual = destino;
        return "";
    }

    private String Rm(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File objetivo = devolverRuta(nombre);
        if (!objetivo.exists()) {
            return "Error: no existe el archivo o carpeta '" + nombre + "'";
        }
        if (SistemaArchivos.esProtegido(objetivo)) {
            return errorProtegido(nombre);
        }
        boolean fueEliminado = SistemaArchivos.eliminarRecursivo(objetivo);
        return fueEliminado ? "'" + nombre + "' eliminado correctamente."
                : "Error: no se pudo eliminar '" + nombre + "'";
    }

    private String Dir() {
        File[] items = actual.listFiles();
        if (items == null || items.length == 0) {
            return "La carpeta esta vacia.";
        }
        ListaEnlazada<File> ordenados = new ListaEnlazada<File>();
        for (File item : items) {
            ordenados.insertarOrdenado(item, comparadorArchivos());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Contenido de ").append(actual.getAbsolutePath()).append(":\n\n");
        int carpetas = 0;
        int archivos = 0;
        for (int i = 0; i < ordenados.tamanio(); i++) {
            File f = ordenados.obtener(i);
            if (f.isDirectory()) {
                sb.append(String.format("[DIR]  %s%n", f.getName()));
                carpetas++;
            } else {
                sb.append(String.format("       %-30s (%d bytes)%n", f.getName(), f.length()));
                archivos++;
            }
        }
        sb.append("\n").append(carpetas).append(" carpeta(s), ").append(archivos).append(" archivo(s).");
        return sb.toString();
    }

    private Comparator<File> comparadorArchivos() {
        return SistemaArchivos.comparadorPorNombre();
    }

    private String Date() {
        LocalDate d = LocalDate.now();
        return "Fecha actual: " + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String Time() {
        LocalTime t = LocalTime.now();
        return "Hora actual: " + t.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String Wr(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo = devolverRuta(nombre);
        if (SistemaArchivos.esArchivoDelSistema(archivo)) {
            return errorProtegido(nombre);
        }
        if (archivo.isDirectory()) {
            return "Error: '" + nombre + "' es una carpeta, no se puede escribir en ella.";
        }
        modoEscritura = true;
        modoAgregar = false;
        archivoEscritura = archivo;
        lineasEscritura = new ArrayList<String>();
        return "Escribiendo en '" + nombre + "'. Escriba EXIT para terminar y guardar.";
    }

    private String Ap(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo = devolverRuta(nombre);
        if (SistemaArchivos.esArchivoDelSistema(archivo)) {
            return errorProtegido(nombre);
        }
        if (archivo.isDirectory()) {
            return "Error: '" + nombre + "' es una carpeta, no se puede escribir en ella.";
        }
        modoEscritura = true;
        modoAgregar = true;
        archivoEscritura = archivo;
        lineasEscritura = new ArrayList<String>();
        return "Agregando texto al final de '" + nombre + "'. Escriba EXIT para terminar y guardar.";
    }

    private String escribirLinea(String linea) {
        if (!linea.equals("EXIT")) {
            lineasEscritura.add(linea);
            return "";
        }
        String nombre = archivoEscritura.getName();
        String resultado;
        try {
            guardarLineas();
            resultado = "Archivo '" + nombre + "' guardado correctamente.";
        } catch (IOException e) {
            resultado = "Error al guardar el archivo: " + e.getMessage();
        } catch (ArchivoCorruptoException e) {
            resultado = "Error: " + e.getMessage();
        }
        modoEscritura = false;
        archivoEscritura = null;
        lineasEscritura = null;
        return resultado;
    }

    private void guardarLineas() throws IOException, ArchivoCorruptoException {
        DocumentoTexto existente = ArchivoBinario.leerDocumentoTexto(archivoEscritura);
        if (existente == null) {
            FileWriter fw = new FileWriter(archivoEscritura, modoAgregar);
            try {
                for (String l : lineasEscritura) {
                    fw.write(l);
                    fw.write(System.lineSeparator());
                }
            } finally {
                fw.close();
            }
            return;
        }
        String texto = String.join("\n", lineasEscritura);
        if (modoAgregar && !existente.getTexto().isEmpty()) {
            texto = existente.getTexto() + "\n" + texto;
        }
        DocumentoTexto actualizado = new DocumentoTexto(texto, existente.getNombreFuente(),
                existente.getTamanioFuente(), existente.getColorRGB());
        ArchivoBinario.guardarObjeto(archivoEscritura.getPath(), actualizado);
    }

    private String leerTexto(File archivo) throws IOException, ArchivoCorruptoException {
        DocumentoTexto documento = ArchivoBinario.leerDocumentoTexto(archivo);
        if (documento != null) {
            return documento.getTexto();
        }
        return ArchivoBinario.leerTextoPlano(archivo);
    }

    private String Rd(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo = devolverRuta(nombre);
        if (!archivo.exists() || archivo.isDirectory()) {
            return "Error: el archivo '" + nombre + "' no existe.";
        }
        String contenido;
        try {
            contenido = leerTexto(archivo);
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        } catch (ArchivoCorruptoException e) {
            return "Error: " + e.getMessage();
        }
        if (contenido.isEmpty()) {
            return "(El archivo esta vacio)";
        }
        return contenido;
    }

    private String Ren(String resto) {
        int espacio = resto.indexOf(' ');
        if (espacio == -1) {
            return "Uso: Ren <actual> <nuevo>";
        }
        String nombreActual = resto.substring(0, espacio);
        String nombreNuevo = resto.substring(espacio + 1).trim();
        if (!nombreValido(nombreActual) || !nombreValido(nombreNuevo)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File origen = devolverRuta(nombreActual);
        File destino = devolverRuta(nombreNuevo);
        if (!origen.exists()) {
            return "Error: no existe '" + nombreActual + "'.";
        }
        if (SistemaArchivos.esProtegido(origen)) {
            return errorProtegido(nombreActual);
        }
        if (SistemaArchivos.esArchivoDelSistema(destino)) {
            return errorProtegido(nombreNuevo);
        }
        if (destino.exists()) {
            return "Error: ya existe un archivo o carpeta llamado '" + nombreNuevo + "'.";
        }
        boolean fueRenombrado = origen.renameTo(destino);
        return fueRenombrado ? "'" + nombreActual + "' renombrado a '" + nombreNuevo + "'."
                : "Error: no se pudo renombrar '" + nombreActual + "'.";
    }

    private String Copy(String resto) {
        int espacio = resto.indexOf(' ');
        if (espacio == -1) {
            return "Uso: Copy <origen> <destino>";
        }
        String nombreOrigen = resto.substring(0, espacio);
        String nombreDestino = resto.substring(espacio + 1).trim();
        if (!nombreValido(nombreOrigen) || !nombreValido(nombreDestino)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File origen = devolverRuta(nombreOrigen);
        File destino = devolverRuta(nombreDestino);
        if (!origen.exists() || origen.isDirectory()) {
            return "Error: el archivo origen '" + nombreOrigen + "' no existe.";
        }
        if (SistemaArchivos.esArchivoDelSistema(destino)) {
            return errorProtegido(nombreDestino);
        }
        if (destino.exists()) {
            return "Error: ya existe un archivo o carpeta llamado '" + nombreDestino + "'.";
        }
        try {
            SistemaArchivos.copiarArchivo(origen, destino);
        } catch (IOException e) {
            return "Error al copiar el archivo: " + e.getMessage();
        }
        return "'" + nombreOrigen + "' copiado a '" + nombreDestino + "'.";
    }

    private String Find(String nombre) {
        if (nombre.isEmpty()) {
            return "Uso: Find <nombre>";
        }
        List<String> resultados = new ArrayList<String>();
        buscarRecursivo(actual, nombre.toLowerCase(), resultados);
        if (resultados.isEmpty()) {
            return "No se encontraron coincidencias para '" + nombre + "'.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Resultados para '").append(nombre).append("':\n\n");
        for (String r : resultados) {
            sb.append(r).append("\n");
        }
        sb.append("\n").append(resultados.size()).append(" resultado(s) encontrado(s).");
        return sb.toString();
    }

    private void buscarRecursivo(File carpeta, String textoBuscado, List<String> resultados) {
        File[] items = carpeta.listFiles();
        if (items == null) {
            return;
        }
        for (File f : items) {
            if (f.getName().toLowerCase().contains(textoBuscado)) {
                resultados.add(f.getAbsolutePath());
            }
            if (f.isDirectory()) {
                buscarRecursivo(f, textoBuscado, resultados);
            }
        }
    }

    private String Info(String nombre) {
        if (!nombreValido(nombre)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File objetivo = devolverRuta(nombre);
        if (!objetivo.exists()) {
            return "Error: no existe '" + nombre + "'.";
        }
        String tipo = objetivo.isDirectory() ? "Carpeta" : "Archivo";
        long tamano = objetivo.isDirectory() ? SistemaArchivos.tamanoRecursivo(objetivo) : objetivo.length();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fecha = sdf.format(new Date(objetivo.lastModified()));
        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(objetivo.getName()).append("\n");
        sb.append("Tipo: ").append(tipo).append("\n");
        sb.append("Ruta: ").append(objetivo.getAbsolutePath()).append("\n");
        sb.append("Tamano: ").append(tamano).append(" bytes\n");
        sb.append("Ultima modificacion: ").append(fecha);
        return sb.toString();
    }

    private String Tree() {
        StringBuilder sb = new StringBuilder();
        sb.append("Estructura de ").append(actual.getAbsolutePath()).append(":\n");
        sb.append(actual.getName()).append("\n");
        arbolRecursivo(actual, "", sb);
        return sb.toString();
    }

    private void arbolRecursivo(File carpeta, String prefijo, StringBuilder sb) {
        File[] items = carpeta.listFiles();
        if (items == null) {
            return;
        }
        ListaEnlazada<File> ordenados = new ListaEnlazada<File>();
        for (File item : items) {
            ordenados.insertarOrdenado(item, comparadorArchivos());
        }
        for (int i = 0; i < ordenados.tamanio(); i++) {
            File f = ordenados.obtener(i);
            boolean esUltimo = (i == ordenados.tamanio() - 1);
            sb.append(prefijo).append(esUltimo ? "\\-- " : "|-- ").append(f.getName()).append("\n");
            if (f.isDirectory()) {
                arbolRecursivo(f, prefijo + (esUltimo ? "    " : "|   "), sb);
            }
        }
    }

    private String Help() {
        return String.join("\n",
                "Comandos disponibles:",
                "  Mkdir <nombre>            - Crea una carpeta",
                "  Mfile <nombre.ext>        - Crea un archivo",
                "  Rm <nombre>               - Elimina un archivo o carpeta",
                "  Cd <nombre carpeta>       - Cambia a la carpeta indicada",
                "  .. / cd..                 - Regresa a la carpeta anterior",
                "  Dir                       - Lista el contenido de la carpeta actual",
                "  Date                      - Muestra la fecha actual",
                "  Time                      - Muestra la hora actual",
                "  Wr <archivo.ext>          - Escribe texto en un archivo (EXIT para terminar)",
                "  Rd <archivo.ext>          - Lee el contenido de un archivo",
                "  Ap <archivo.ext>          - Agrega texto al final de un archivo (EXIT para terminar)",
                "  Ren <actual> <nuevo>      - Renombra un archivo o carpeta",
                "  Copy <origen> <destino>   - Copia un archivo",
                "  Find <nombre>             - Busca archivos o carpetas por nombre",
                "  Info <nombre>             - Muestra informacion de un archivo o carpeta",
                "  Tree                      - Muestra el arbol de carpetas y archivos",
                "  Cls                       - Limpia la pantalla",
                "  Help                      - Muestra esta ayuda",
                "  Exit                      - Cierra la consola",
                "  Grep <texto> <archivo>    - Busca texto dentro de un archivo"
        );
    }

    private String Grep(String resto) {
        String[] tokens = resto.isEmpty() ? new String[0] : resto.trim().split("\\s+");
        if (tokens.length < 2) {
            return "Uso: Grep <texto> <archivo.ext>";
        }
        String nombreArchivo = tokens[tokens.length - 1];
        StringBuilder textoBuilder = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            if (i > 0) {
                textoBuilder.append(" ");
            }
            textoBuilder.append(tokens[i]);
        }
        String texto = textoBuilder.toString();
        if (!nombreValido(nombreArchivo)) {
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo = devolverRuta(nombreArchivo);
        if (!archivo.exists() || archivo.isDirectory()) {
            return "Error: el archivo '" + nombreArchivo + "' no existe.";
        }
        String contenido;
        try {
            contenido = leerTexto(archivo);
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        } catch (ArchivoCorruptoException e) {
            return "Error: " + e.getMessage();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Buscando \"").append(texto).append("\" en ").append(nombreArchivo).append("...\n");
        int contador = 0;
        String textoMinuscula = texto.toLowerCase();
        String[] lineas = contenido.split("\n");
        for (int i = 0; i < lineas.length; i++) {
            if (lineas[i].toLowerCase().contains(textoMinuscula)) {
                sb.append("Linea ").append(i + 1).append(": ").append(lineas[i]).append("\n");
                contador++;
            }
        }
        if (contador == 0) {
            return "No se encontraron coincidencias de \"" + texto + "\" en " + nombreArchivo + ".";
        }
        sb.append(contador).append(" coincidencia(s) encontrada(s).");
        return sb.toString();
    }
}
