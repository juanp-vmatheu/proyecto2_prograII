package insta;

import insta.ListaEnlazada;
import insta.ArchivoCorruptoException;
import insta.CuentaDesactivadaException;
import insta.UsernameDuplicadoException;
import insta.GestorArchivos;
import insta.RutasInsta;
import insta.Mensaje;
import insta.Publicacion;
import insta.Sticker;
import insta.TipoMensaje;
import insta.Usuario; 

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class NucleoInsta {

    private static final int MAX_CARACTERES_PUBLICACION = 220;
    private static final int MAX_CARACTERES_MENSAJE = 300;

    private NucleoInsta() {
    }

    // ---------- Inicializacion ----------

    public static synchronized void inicializar() throws ArchivoCorruptoException {
        File raiz = new File(RutasInsta.RAIZ);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
        crearStickersGlobales();
        if (!GestorArchivos.existe(RutasInsta.ARCHIVO_USUARIOS)) {
            GestorArchivos.guardar(RutasInsta.ARCHIVO_USUARIOS, new ListaEnlazada<Usuario>());
            crearCuentasDeEjemplo();
        }
    }

    private static void crearStickersGlobales() {
        File carpeta = new File(RutasInsta.STICKERS_GLOBALES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        generarStickerSiNoExiste("feliz.png", "FE", new Color(255, 221, 87));
        generarStickerSiNoExiste("triste.png", "TR", new Color(120, 170, 255));
        generarStickerSiNoExiste("corazon.png", "CO", new Color(255, 99, 132));
        generarStickerSiNoExiste("risa.png", "RI", new Color(255, 193, 7));
        generarStickerSiNoExiste("aplauso.png", "AP", new Color(153, 204, 255));
    }

    private static void generarStickerSiNoExiste(String nombreArchivo, String texto, Color color) {
        File archivo = new File(RutasInsta.STICKERS_GLOBALES, nombreArchivo);
        if (archivo.exists()) {
            return;
        }
        try {
            int tamano = 96;
            BufferedImage imagen = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagen.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillOval(4, 4, tamano - 8, tamano - 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            FontMetrics metrica = g.getFontMetrics();
            int x = (tamano - metrica.stringWidth(texto)) / 2;
            int y = (tamano - metrica.getHeight()) / 2 + metrica.getAscent();
            g.drawString(texto, x, y);
            g.dispose();
            ImageIO.write(imagen, "png", archivo);
        } catch (IOException e) {
            System.out.println("No se pudo generar el sticker " + nombreArchivo);
        }
    }

    private static void crearCuentasDeEjemplo() throws ArchivoCorruptoException {
        String[][] cuentas = {
                {"noticiashn", "Noticias HN", "M"},
                {"modaydeporte", "Moda y Deporte", "F"},
                {"entretenimientoplus", "Entretenimiento Plus", "F"}
        };
        String[][] publicaciones = {
                {"noticiashn", "Ultima hora: la economia crece este trimestre #noticias"},
                {"noticiashn", "Clima para hoy: soleado en todo el pais #clima"},
                {"modaydeporte", "Los mejores looks de la temporada #moda"},
                {"modaydeporte", "Resumen de la jornada deportiva #deporte"},
                {"entretenimientoplus", "Estreno de la semana en cines #entretenimiento"},
                {"entretenimientoplus", "Top 5 series del momento #series"}
        };
        for (String[] cuenta : cuentas) {
            try {
                registrarUsuario(cuenta[1], cuenta[2].charAt(0), cuenta[0], "1234", 25, null);
            } catch (UsernameDuplicadoException e) {
                // ya existe, se ignora
            }
        }
        for (String[] p : publicaciones) {
            publicar(p[0], p[1], null, null);
        }
    }

    // ---------- Autenticacion y perfil ----------

    @SuppressWarnings("unchecked")
    private static ListaEnlazada<Usuario> cargarUsuarios() throws ArchivoCorruptoException {
        ListaEnlazada<Usuario> usuarios = GestorArchivos.cargar(RutasInsta.ARCHIVO_USUARIOS);
        return usuarios == null ? new ListaEnlazada<>() : usuarios;
    }

    public static synchronized Usuario login(String username, String password) throws CuentaDesactivadaException, ArchivoCorruptoException {
        for (Usuario u : cargarUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                if (!u.isActiva()) {
                    throw new CuentaDesactivadaException("La cuenta de " + username + " esta desactivada");
                }
                return u;
            }
        }
        return null;
    }

    public static synchronized void registrarUsuario(String nombreCompleto, char genero, String username, String password,
                                                       int edad, String fotoPerfil) throws UsernameDuplicadoException, ArchivoCorruptoException {
        ListaEnlazada<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                throw new UsernameDuplicadoException("El username " + username + " ya existe");
            }
        }
        usuarios.agregarFinal(new Usuario(nombreCompleto, genero, username, password, edad, fotoPerfil));
        GestorArchivos.guardar(RutasInsta.ARCHIVO_USUARIOS, usuarios);

        crearCarpetasUsuario(username);
        GestorArchivos.guardar(RutasInsta.archivoFollowing(username), new ListaEnlazada<String>());
        GestorArchivos.guardar(RutasInsta.archivoFollowers(username), new ListaEnlazada<String>());
        GestorArchivos.guardar(RutasInsta.archivoInsta(username), new ListaEnlazada<Publicacion>());
        GestorArchivos.guardar(RutasInsta.archivoInbox(username), new ListaEnlazada<Mensaje>());

        ListaEnlazada<Sticker> stickers = new ListaEnlazada<>();
        stickers.agregarFinal(new Sticker("Feliz", RutasInsta.STICKERS_GLOBALES + File.separator + "feliz.png", false));
        stickers.agregarFinal(new Sticker("Triste", RutasInsta.STICKERS_GLOBALES + File.separator + "triste.png", false));
        stickers.agregarFinal(new Sticker("Corazon", RutasInsta.STICKERS_GLOBALES + File.separator + "corazon.png", false));
        stickers.agregarFinal(new Sticker("Risa", RutasInsta.STICKERS_GLOBALES + File.separator + "risa.png", false));
        stickers.agregarFinal(new Sticker("Aplauso", RutasInsta.STICKERS_GLOBALES + File.separator + "aplauso.png", false));
        GestorArchivos.guardar(RutasInsta.archivoStickers(username), stickers);
    }

    private static void crearCarpetasUsuario(String username) {
        new File(RutasInsta.carpetaUsuario(username)).mkdirs();
        new File(RutasInsta.carpetaImagenes(username)).mkdirs();
        new File(RutasInsta.carpetaFoldersPersonales(username)).mkdirs();
        new File(RutasInsta.carpetaStickersPersonales(username)).mkdirs();
    }

    public static synchronized Usuario buscarUsuario(String username) throws ArchivoCorruptoException {
        for (Usuario u : cargarUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public static synchronized void actualizarPerfil(String username, String nombreCompleto, String fotoPerfil) throws ArchivoCorruptoException {
        ListaEnlazada<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                    u.setNombreCompleto(nombreCompleto);
                }
                if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                    u.setFotoPerfil(fotoPerfil);
                }
                GestorArchivos.guardar(RutasInsta.ARCHIVO_USUARIOS, usuarios);
                return;
            }
        }
    }

    public static synchronized boolean activarDesactivarCuenta(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                u.setActiva(!u.isActiva());
                GestorArchivos.guardar(RutasInsta.ARCHIVO_USUARIOS, usuarios);
                return u.isActiva();
            }
        }
        return false;
    }

    // ---------- Seguidores ----------

    @SuppressWarnings("unchecked")
    public static synchronized ListaEnlazada<String> obtenerFollowing(String username) throws ArchivoCorruptoException {
        ListaEnlazada<String> lista = GestorArchivos.cargar(RutasInsta.archivoFollowing(username));
        return lista == null ? new ListaEnlazada<>() : lista;
    }

    @SuppressWarnings("unchecked")
    public static synchronized ListaEnlazada<String> obtenerFollowers(String username) throws ArchivoCorruptoException {
        ListaEnlazada<String> lista = GestorArchivos.cargar(RutasInsta.archivoFollowers(username));
        return lista == null ? new ListaEnlazada<>() : lista;
    }

    public static synchronized void seguir(String miUsername, String objetivoUsername) throws ArchivoCorruptoException {
        if (miUsername.equalsIgnoreCase(objetivoUsername)) {
            return;
        }
        ListaEnlazada<String> siguiendo = obtenerFollowing(miUsername);
        if (!siguiendo.contiene(objetivoUsername)) {
            siguiendo.agregarFinal(objetivoUsername);
            GestorArchivos.guardar(RutasInsta.archivoFollowing(miUsername), siguiendo);
        }
        ListaEnlazada<String> followersObjetivo = obtenerFollowers(objetivoUsername);
        if (!followersObjetivo.contiene(miUsername)) {
            followersObjetivo.agregarFinal(miUsername);
            GestorArchivos.guardar(RutasInsta.archivoFollowers(objetivoUsername), followersObjetivo);
        }
    }

    public static synchronized void dejarDeSeguir(String miUsername, String objetivoUsername) throws ArchivoCorruptoException {
        ListaEnlazada<String> siguiendo = obtenerFollowing(miUsername);
        siguiendo.eliminar(objetivoUsername);
        GestorArchivos.guardar(RutasInsta.archivoFollowing(miUsername), siguiendo);

        ListaEnlazada<String> followersObjetivo = obtenerFollowers(objetivoUsername);
        followersObjetivo.eliminar(miUsername);
        GestorArchivos.guardar(RutasInsta.archivoFollowers(objetivoUsername), followersObjetivo);
    }

    // ---------- Publicaciones ----------

    @SuppressWarnings("unchecked")
    public static synchronized void publicar(String username, String contenido, String imagenRuta, String stickerRuta) throws ArchivoCorruptoException {
        if (contenido != null && contenido.length() > MAX_CARACTERES_PUBLICACION) {
            contenido = contenido.substring(0, MAX_CARACTERES_PUBLICACION);
        }
        ListaEnlazada<Publicacion> publicaciones = GestorArchivos.cargar(RutasInsta.archivoInsta(username));
        if (publicaciones == null) {
            publicaciones = new ListaEnlazada<>();
        }
        publicaciones.agregarInicio(new Publicacion(username, contenido, imagenRuta, stickerRuta));
        GestorArchivos.guardar(RutasInsta.archivoInsta(username), publicaciones);
    }

    @SuppressWarnings("unchecked")
    public static synchronized ListaEnlazada<Publicacion> publicacionesDe(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Publicacion> publicaciones = GestorArchivos.cargar(RutasInsta.archivoInsta(username));
        return publicaciones == null ? new ListaEnlazada<>() : publicaciones;
    }

    public static synchronized ListaEnlazada<Publicacion> timeline(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        for (Publicacion p : publicacionesDe(username)) {
            resultado.agregarFinal(p);
        }
        for (String seguido : obtenerFollowing(username)) {
            Usuario u = buscarUsuario(seguido);
            if (u != null && u.isActiva()) {
                for (Publicacion p : publicacionesDe(seguido)) {
                    resultado.agregarFinal(p);
                }
            }
        }
        return ordenarPorFechaDesc(resultado);
    }

    public static synchronized ListaEnlazada<Publicacion> interacciones(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        String mencion = "@" + username.toLowerCase();
        for (Usuario u : cargarUsuarios()) {
            if (!u.isActiva()) {
                continue;
            }
            for (Publicacion p : publicacionesDe(u.getUsername())) {
                if (p.getContenido() != null && p.getContenido().toLowerCase().contains(mencion)
                        && !contienePublicacion(resultado, p)) {
                    resultado.agregarFinal(p);
                }
            }
        }
        return ordenarPorFechaDesc(resultado);
    }

    public static synchronized ListaEnlazada<Publicacion> buscarPorHashtag(String hashtag) throws ArchivoCorruptoException {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        String tag = (hashtag.startsWith("#") ? hashtag : "#" + hashtag).toLowerCase();
        for (Usuario u : cargarUsuarios()) {
            if (!u.isActiva()) {
                continue;
            }
            for (Publicacion p : publicacionesDe(u.getUsername())) {
                if (p.getContenido() != null && p.getContenido().toLowerCase().contains(tag)
                        && !contienePublicacion(resultado, p)) {
                    resultado.agregarFinal(p);
                }
            }
        }
        return resultado;
    }

    private static boolean contienePublicacion(ListaEnlazada<Publicacion> lista, Publicacion p) {
        for (Publicacion existente : lista) {
            if (existente.getAutor().equals(p.getAutor()) && existente.getFecha().equals(p.getFecha())) {
                return true;
            }
        }
        return false;
    }

    private static ListaEnlazada<Publicacion> ordenarPorFechaDesc(ListaEnlazada<Publicacion> lista) {
        ListaEnlazada<Publicacion> restante = new ListaEnlazada<>();
        for (Publicacion p : lista) {
            restante.agregarFinal(p);
        }
        ListaEnlazada<Publicacion> ordenada = new ListaEnlazada<>();
        while (!restante.estaVacia()) {
            Publicacion masReciente = restante.obtener(0);
            for (int i = 1; i < restante.tamano(); i++) {
                Publicacion actual = restante.obtener(i);
                if (actual.getFecha().isAfter(masReciente.getFecha())) {
                    masReciente = actual;
                }
            }
            ordenada.agregarFinal(masReciente);
            restante.eliminar(masReciente);
        }
        return ordenada;
    }

    // ---------- Busqueda ----------

    public static synchronized ListaEnlazada<Usuario> buscarPersonas(String texto) throws ArchivoCorruptoException {
        ListaEnlazada<Usuario> resultado = new ListaEnlazada<>();
        String textoBuscado = texto.toLowerCase();
        for (Usuario u : cargarUsuarios()) {
            if (u.isActiva() && u.getUsername().toLowerCase().contains(textoBuscado)) {
                resultado.agregarFinal(u);
            }
        }
        return resultado;
    }

    // ---------- Inbox ----------

    @SuppressWarnings("unchecked")
    private static ListaEnlazada<Mensaje> cargarInbox(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Mensaje> mensajes = GestorArchivos.cargar(RutasInsta.archivoInbox(username));
        return mensajes == null ? new ListaEnlazada<>() : mensajes;
    }

    public static synchronized void enviarMensaje(String emisor, String receptor, String contenido, TipoMensaje tipo) throws ArchivoCorruptoException {
        if (contenido != null && contenido.length() > MAX_CARACTERES_MENSAJE) {
            contenido = contenido.substring(0, MAX_CARACTERES_MENSAJE);
        }
        Mensaje mensaje = new Mensaje(emisor, receptor, contenido, tipo);

        ListaEnlazada<Mensaje> inboxEmisor = cargarInbox(emisor);
        inboxEmisor.agregarFinal(mensaje);
        GestorArchivos.guardar(RutasInsta.archivoInbox(emisor), inboxEmisor);

        ListaEnlazada<Mensaje> inboxReceptor = cargarInbox(receptor);
        inboxReceptor.agregarFinal(mensaje);
        GestorArchivos.guardar(RutasInsta.archivoInbox(receptor), inboxReceptor);
    }

    public static synchronized ListaEnlazada<Mensaje> conversacion(String usuario, String otroUsuario) throws ArchivoCorruptoException {
        ListaEnlazada<Mensaje> resultado = new ListaEnlazada<>();
        for (Mensaje m : cargarInbox(usuario)) {
            if ((m.getEmisor().equalsIgnoreCase(usuario) && m.getReceptor().equalsIgnoreCase(otroUsuario))
                    || (m.getEmisor().equalsIgnoreCase(otroUsuario) && m.getReceptor().equalsIgnoreCase(usuario))) {
                resultado.agregarFinal(m);
            }
        }
        return resultado;
    }

    public static synchronized ListaEnlazada<String> listarConversaciones(String username) throws ArchivoCorruptoException {
        ListaEnlazada<String> resultado = new ListaEnlazada<>();
        for (Mensaje m : cargarInbox(username)) {
            String otro = m.getEmisor().equalsIgnoreCase(username) ? m.getReceptor() : m.getEmisor();
            if (!resultado.contiene(otro)) {
                resultado.agregarFinal(otro);
            }
        }
        return resultado;
    }

    public static synchronized void marcarConversacionLeida(String usuario, String otroUsuario) throws ArchivoCorruptoException {
        ListaEnlazada<Mensaje> inbox = cargarInbox(usuario);
        for (Mensaje m : inbox) {
            if (m.getReceptor().equalsIgnoreCase(usuario) && m.getEmisor().equalsIgnoreCase(otroUsuario)) {
                m.marcarLeido();
            }
        }
        GestorArchivos.guardar(RutasInsta.archivoInbox(usuario), inbox);
    }

    public static synchronized void eliminarConversacion(String usuario, String otroUsuario) throws ArchivoCorruptoException {
        ListaEnlazada<Mensaje> restante = new ListaEnlazada<>();
        for (Mensaje m : cargarInbox(usuario)) {
            boolean esDeEstaConversacion = (m.getEmisor().equalsIgnoreCase(usuario) && m.getReceptor().equalsIgnoreCase(otroUsuario))
                    || (m.getEmisor().equalsIgnoreCase(otroUsuario) && m.getReceptor().equalsIgnoreCase(usuario));
            if (!esDeEstaConversacion) {
                restante.agregarFinal(m);
            }
        }
        GestorArchivos.guardar(RutasInsta.archivoInbox(usuario), restante);
    }

    public static synchronized boolean hayMensajesNuevos(String usuario) throws ArchivoCorruptoException {
        for (Mensaje m : cargarInbox(usuario)) {
            if (m.getReceptor().equalsIgnoreCase(usuario) && !m.isLeido()) {
                return true;
            }
        }
        return false;
    }

    // ---------- Stickers ----------

    @SuppressWarnings("unchecked")
    public static synchronized ListaEnlazada<Sticker> stickersDisponibles(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Sticker> stickers = GestorArchivos.cargar(RutasInsta.archivoStickers(username));
        return stickers == null ? new ListaEnlazada<>() : stickers;
    }

    public static synchronized void importarSticker(String username, String nombre, String rutaOrigen) throws ArchivoCorruptoException, IOException {
        String extensionOrigen = rutaOrigen.toLowerCase();
        if (!extensionOrigen.endsWith(".png") && !extensionOrigen.endsWith(".jpg") && !extensionOrigen.endsWith(".jpeg")) {
            throw new IllegalArgumentException("El sticker debe ser .png o .jpg");
        }
        String extension = extensionOrigen.substring(extensionOrigen.lastIndexOf('.'));
        File destino = new File(RutasInsta.carpetaStickersPersonales(username), nombre + extension);
        Files.copy(Paths.get(rutaOrigen), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ListaEnlazada<Sticker> stickers = stickersDisponibles(username);
        stickers.agregarFinal(new Sticker(nombre, destino.getPath(), true));
        GestorArchivos.guardar(RutasInsta.archivoStickers(username), stickers);
    }
}