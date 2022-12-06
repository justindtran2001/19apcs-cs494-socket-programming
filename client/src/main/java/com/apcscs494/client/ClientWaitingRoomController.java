package com.apcscs494.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ClientWaitingRoomController implements Initializable {
    @FXML
    Pane rootPane;

    Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }

        client.waitForGameStart(rootPane);

        Platform.runLater(() -> rootPane.getScene().getWindow().setOnCloseRequest(windowEvent -> client.exit()));
    }

    public static void startGame(Pane rootPane) {
        Platform.runLater(() -> {
            try {
                Scene scene = new Scene(
                        FXMLLoader.load(
                                Objects.requireNonNull(ClientApp.class.getResource("client-app.fxml"))
                        )
                );
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                System.out.println("Error at waiting room: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
}
