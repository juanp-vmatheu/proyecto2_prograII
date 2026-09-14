package insta;

import java.io.File;

public class RutasInsta {

    public static final String RAIZ = "Z" + File.separator + "INSTA_RAIZ";
    public static final String ARCHIVO_USUARIOS = RAIZ + File.separator + "users.ins";
    public static final String STICKERS_GLOBALES = RAIZ + File.separator + "stickers_globales";

    private RutasInsta() {
    }

    public static String carpetaUsuario(String username) {
        return RAIZ + File.separator + username;
    }

    public static String archivoFollowing(String username) {
        return carpetaUsuario(username) + File.separator + "following.ins";
    }

    public static String archivoFollowers(String username) {
        return carpetaUsuario(username) + File.separator + "followers.ins";
    }

    public static String archivoInsta(String username) {
        return carpetaUsuario(username) + File.separator + "insta.ins";
    }

    public static String archivoInbox(String username) {
        return carpetaUsuario(username) + File.separator + "inbox.ins";
    }

    public static String archivoStickers(String username) {
        return carpetaUsuario(username) + File.separator + "stickers.ins";
    }

    public static String carpetaImagenes(String username) {
        return carpetaUsuario(username) + File.separator + "imagenes";
    }

    public static String carpetaFoldersPersonales(String username) {
        return carpetaUsuario(username) + File.separator + "folders_personales";
    }

    public static String carpetaStickersPersonales(String username) {
        return carpetaUsuario(username) + File.separator + "stickers_personales";
    }
}