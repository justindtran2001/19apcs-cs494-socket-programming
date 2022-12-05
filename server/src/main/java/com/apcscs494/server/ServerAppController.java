package com.apcscs494.server;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
            server = new Server(new ServerSocket(1234, 10));
        } catch (IOException e) {
            System.out.println("Error creating a server");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            server.acceptingPlayers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        startGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("startGameButton clicked");
                if (Utility.hasEnoughPlayers(Player.players)) {
                    startNewGame();
                } else {
                    // TODO: Alert that not enough players
                }
            }
        });

        endGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("endGameButton clicked");
                Player.game.endByAdmin();
                server.notifyEndGame();
            }
        });
    }

    private void startNewGame() {
        Player.game.restart();
        server.broadcastAllToStartGame();
        server.sendScoreboard();
        server.sendQuestion();
        server.chooseNextPlayer();
    }


}
