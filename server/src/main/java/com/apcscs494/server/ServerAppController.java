package com.apcscs494.server;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;


public class ServerAppController implements Initializable {
    @FXML
    AnchorPane rootPane;
    @FXML
    Button startGameButton;
    @FXML
    Button endGameButton;
    @FXML
    Text responseText;
    @FXML
    Text numOfPlayerText;
    @FXML
    Button refreshButton;

    private Server server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            server = new Server(new ServerSocket(Server.PORT, 10));
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

        responseText.setText("");

        startGameButton.setOnAction(actionEvent -> {
            System.out.println("startGameButton clicked");
            if (Utility.hasEnoughPlayers(Player.players)) {
                startNewGame();
            } else {
                System.out.println("Not enough players!!");
                responseText.setText("Not enough players.\n Number of players needs to be between 2 and 10. ");
            }
        });

        endGameButton.setOnAction(actionEvent -> {
            System.out.println("endGameButton clicked");
            Player.game.endByAdmin();
            server.notifyEndGame();
        });

        refreshButton.setOnAction(actionEvent -> {
            System.out.println("refreshButton clicked");
            updateNumOfPlayersText();
        });

        Platform.runLater(() -> rootPane.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent));
    }

    private <T extends Event> void closeWindowEvent(T t) {
        server.exit();
    }

    private void startNewGame() {
        Player.game.restart();
        server.broadcastAllToStartGame();
        server.sendScoreboard();
        server.sendQuestion();
        server.chooseNextPlayer();
    }

    void updateNumOfPlayersText() {
        Platform.runLater(() -> numOfPlayerText.setText(String.valueOf(Player.players.size())));
    }

}
