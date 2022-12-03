package com.apcscs494.server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerAppController implements Initializable {
    @FXML
    Button startGameButton;
    @FXML
    Button endGameButton;

    private Server server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            server = new Server(new ServerSocket(8386, 10));
        } catch (IOException e) {
            System.out.println("Error creating a server");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            server.acceptingPlayers();
            server.listenMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
