package insta;

import insta.ListaEnlazada;
import insta.ArchivoCorruptoException;
import insta.CuentaDesactivadaException;
import insta.UsernameDuplicadoException;
import insta.Usuario;
import insta.NucleoInsta;
import insta.Protocolo;
import insta.Respuesta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ManejadorCliente implements Runnable {

    private final Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             ObjectOutputStream salida = new ObjectOutputStream(s.getOutputStream())) {

            String linea = entrada.readLine();
            Respuesta respuesta = procesar(linea);
            salida.writeObject(respuesta);
            salida.flush();

        } catch (IOException e) {
            System.out.println("Error atendiendo cliente: " + e.getMessage());
        }
    }

    private Respuesta procesar(String linea) {
        if (linea == null || linea.isEmpty()) {
            return Respuesta.error("Comando vacio");
        }
        String comando = linea.split(Protocolo.SEPARADOR, 2)[0];

        try {
            switch (comando) {
                case Protocolo.LOGIN: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    Usuario u = NucleoInsta.login(p[1], p[2]);
                    return u == null ? Respuesta.error("Usuario o contraseña incorrectos") : Respuesta.ok(u);
                }
                case Protocolo.REGISTRO: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 7);
                    NucleoInsta.registrarUsuario(p[1], p[2].charAt(0), p[3], p[4],
                            Integer.parseInt(p[5]), p[6].isEmpty() ? null : p[6]);
                    return Respuesta.ok(null);
                }
                case Protocolo.PERFIL: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    Usuario u = NucleoInsta.buscarUsuario(p[1]);
                    if (u == null) {
                        return Respuesta.error("Usuario no encontrado");
                    }
                    Object[] datos = new Object[]{
                            u,
                            NucleoInsta.obtenerFollowers(p[1]).tamano(),
                            NucleoInsta.obtenerFollowing(p[1]).tamano(),
                            NucleoInsta.publicacionesDe(p[1]).tamano()
                    };
                    return Respuesta.ok(datos);
                }
                case Protocolo.ACTUALIZAR_PERFIL: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 4);
                    NucleoInsta.actualizarPerfil(p[1], p[2], p[3]);
                    return Respuesta.ok(null);
                }
                case Protocolo.SEGUIR: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    NucleoInsta.seguir(p[1], p[2]);
                    return Respuesta.ok(null);
                }
                case Protocolo.DEJAR_SEGUIR: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    NucleoInsta.dejarDeSeguir(p[1], p[2]);
                    return Respuesta.ok(null);
                }
                case Protocolo.FOLLOWERS: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.obtenerFollowers(p[1]));
                }
                case Protocolo.FOLLOWING: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.obtenerFollowing(p[1]));
                }
                case Protocolo.PUBLICAR: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 5);
                    NucleoInsta.publicar(p[1], p[4], p[2].isEmpty() ? null : p[2], p[3].isEmpty() ? null : p[3]);
                    return Respuesta.ok(null);
                }
                case Protocolo.TIMELINE: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.timeline(p[1]));
                }
                case Protocolo.PUBLICACIONES_DE: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.publicacionesDe(p[1]));
                }
                case Protocolo.INTERACCIONES: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.interacciones(p[1]));
                }
                case Protocolo.BUSCAR_PERSONAS: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    ListaEnlazada<Usuario> encontrados = NucleoInsta.buscarPersonas(p[2]);
                    ListaEnlazada<String> siguiendo = NucleoInsta.obtenerFollowing(p[1]);
                    return Respuesta.ok(new Object[]{encontrados, siguiendo});
                }
                case Protocolo.BUSCAR_HASHTAG: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.buscarPorHashtag(p[1]));
                }
                case Protocolo.ENVIAR_MENSAJE: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 4);
                    NucleoInsta.enviarMensaje(p[1], p[2], p[3], TipoMensaje.TEXTO);
                    return Respuesta.ok(null); 
                }
                case Protocolo.ENVIAR_STICKER: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 4);
                    NucleoInsta.enviarMensaje(p[1], p[2], p[3], TipoMensaje.STICKER);
                    return Respuesta.ok(null);
                }
                case Protocolo.CONVERSACION: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    return Respuesta.ok(NucleoInsta.conversacion(p[1], p[2]));
                }
                case Protocolo.LISTAR_CONVERSACIONES: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.listarConversaciones(p[1]));
                }
                case Protocolo.MARCAR_LEIDO: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    NucleoInsta.marcarConversacionLeida(p[1], p[2]);
                    return Respuesta.ok(null);
                }
                case Protocolo.ELIMINAR_CONVERSACION: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 3);
                    NucleoInsta.eliminarConversacion(p[1], p[2]);
                    return Respuesta.ok(null);
                }
                case Protocolo.HAY_MENSAJES_NUEVOS: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.hayMensajesNuevos(p[1]));
                }
                case Protocolo.STICKERS_DISPONIBLES: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.stickersDisponibles(p[1]));
                }
                case Protocolo.IMPORTAR_STICKER: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 4);
                    NucleoInsta.importarSticker(p[1], p[2], p[3]);
                    return Respuesta.ok(null);
                }
                case Protocolo.ACTIVAR_DESACTIVAR: {
                    String[] p = linea.split(Protocolo.SEPARADOR, 2);
                    return Respuesta.ok(NucleoInsta.activarDesactivarCuenta(p[1]));
                }
                default:
                    return Respuesta.error("Comando desconocido: " + comando);
            }
        } catch (UsernameDuplicadoException | CuentaDesactivadaException | ArchivoCorruptoException e) {
            return Respuesta.error(e.getMessage());
        } catch (Exception e) {
            return Respuesta.error("Error interno: " + e.getMessage());
        }
    }
}