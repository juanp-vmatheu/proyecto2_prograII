package insta;

import insta.ClienteInsta;
import insta.ListaEnlazada;
import insta.Usuario;
import insta.Protocolo;
import insta.Respuesta;
import miniwindows.apps.EstiloMinecraft;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PanelBuscarProfile extends JPanel {

    private final ClienteInsta cliente;
    private final String miUsername;
    private final JTextField campoBusqueda = new JTextField(20);
    private final DefaultListModel<String> modeloResultados = new DefaultListModel<>();
    private final JList<String> listaResultados = new JList<>(modeloResultados);
    private final List<String> usernamesEncontrados = new ArrayList<>();
    private final PanelPerfil panelPerfilDetalle;
    private String ultimaBusqueda;
    private boolean actualizandoLista;

    public PanelBuscarProfile(ClienteInsta cliente, String miUsername, Consumer<String> alVerPublicaciones) {
        this.cliente = cliente;
        this.miUsername = miUsername;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        EstiloMinecraft.aplicarPanel(this);

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstiloMinecraft.aplicarPanel(superior);
        superior.add(new JLabel("Buscar:"));
        superior.add(campoBusqueda);
        JButton botonBuscar = new JButton("Buscar");
        EstiloMinecraft.aplicarBoton(botonBuscar);
        superior.add(botonBuscar);
        add(superior, BorderLayout.NORTH);

        listaResultados.setBackground(EstiloMinecraft.GRIS_FONDO);
        JScrollPane scrollResultados = new JScrollPane(listaResultados);
        scrollResultados.setPreferredSize(new Dimension(200, 0));
        EstiloMinecraft.aplicarRanura(scrollResultados);
        add(scrollResultados, BorderLayout.WEST);

        panelPerfilDetalle = new PanelPerfil(cliente, miUsername, alVerPublicaciones);
        panelPerfilDetalle.setAlCambiarSeguimiento(this::cargarResultados);
        add(panelPerfilDetalle, BorderLayout.CENTER);

        botonBuscar.addActionListener(e -> buscar());
        campoBusqueda.addActionListener(e -> buscar());
        listaResultados.addListSelectionListener(e -> {
            int indice = listaResultados.getSelectedIndex();
            if (!actualizandoLista && !e.getValueIsAdjusting() && indice >= 0 && indice < usernamesEncontrados.size()) {
                panelPerfilDetalle.mostrarPerfil(usernamesEncontrados.get(indice));
            }
        });
    }

    public void refrescar() {
        if (ultimaBusqueda != null) {
            campoBusqueda.setText(ultimaBusqueda);
            cargarResultados();
        }
        panelPerfilDetalle.refrescar();
    }

    private void buscar() {
        String texto = campoBusqueda.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        ultimaBusqueda = texto;
        cargarResultados();
    }

    @SuppressWarnings("unchecked")
    private void cargarResultados() {
        if (ultimaBusqueda == null) {
            return;
        }
        Respuesta respuesta = cliente.enviar(cliente.armar(Protocolo.BUSCAR_PERSONAS, miUsername, ultimaBusqueda));
        actualizandoLista = true;
        modeloResultados.clear();
        usernamesEncontrados.clear();
        if (!respuesta.isExito()) {
            modeloResultados.addElement("Error: " + respuesta.getMensaje());
        } else {
            Object[] datos = (Object[]) respuesta.getDatos();
            ListaEnlazada<Usuario> encontrados = (ListaEnlazada<Usuario>) datos[0];
            ListaEnlazada<String> siguiendo = (ListaEnlazada<String>) datos[1];
            if (encontrados.estaVacia()) {
                modeloResultados.addElement("Sin resultados.");
            }
            for (Usuario u : encontrados) {
                String etiqueta;
                if (u.getUsername().equalsIgnoreCase(miUsername)) {
                    etiqueta = " - Tu perfil";
                } else if (siguiendo.contiene(u.getUsername())) {
                    etiqueta = " - Lo sigo";
                } else {
                    etiqueta = " - No lo sigues";
                }
                modeloResultados.addElement(u.getUsername() + etiqueta);
                usernamesEncontrados.add(u.getUsername());
            }
        }
        int indiceMostrado = -1;
        for (int i = 0; i < usernamesEncontrados.size(); i++) {
            if (usernamesEncontrados.get(i).equalsIgnoreCase(panelPerfilDetalle.getUsernameMostrado())) {
                indiceMostrado = i;
            }
        }
        if (indiceMostrado >= 0) {
            listaResultados.setSelectedIndex(indiceMostrado);
        }
        actualizandoLista = false;
    }
}
