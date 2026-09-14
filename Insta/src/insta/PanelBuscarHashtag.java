package insta;

import insta.ClienteInsta; 

import javax.swing.*;
import java.awt.*;

public class PanelBuscarHashtag extends JPanel {

    private final JTextField campoHashtag = new JTextField(20);
    private final PanelFeed panelResultados;

    public PanelBuscarHashtag(ClienteInsta cliente) {
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        superior.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        superior.add(new JLabel("Hashtag (sin #):"));
        superior.add(campoHashtag);
        JButton botonBuscar = new JButton("Buscar");
        superior.add(botonBuscar);
        add(superior, BorderLayout.NORTH);

        panelResultados = new PanelFeed(cliente, PanelFeed.Modo.HASHTAG);
        add(panelResultados, BorderLayout.CENTER);

        Runnable buscar = () -> {
            String texto = campoHashtag.getText().trim();
            if (!texto.isEmpty()) {
                panelResultados.cargar(texto);
            }
        };
        botonBuscar.addActionListener(e -> buscar.run());
        campoHashtag.addActionListener(e -> buscar.run());
    }
}