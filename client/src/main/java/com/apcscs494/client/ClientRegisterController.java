package com.apcscs494.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientRegisterController implements Initializable {

    @FXML
    TextField usernameTextField;
    @FXML
    Button registerButton;
    @FXML
    Text serverRespText;

    Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }

        // Set event handlers from UI elements
        usernameTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                registerButton.fire();
            }
        });
        registerButton.setOnAction(event -> {
            registerUsername();

            client.listenForRegistrationConfirm(serverRespText);
        });
    }

    public void registerUsername() {
        try {
            String username = usernameTextField.getText();
            if (username.isEmpty()) return;

            client.registerPlayerByUsername(username);

            usernameTextField.clear();
        } catch (Exception e) {
            System.out.println("Error at clientRegisterController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void handleResponse(String receivedResponse, Text responseText) {
        Platform.runLater(() -> {
            responseText.setText(receivedResponse);
            if (receivedResponse.contains("SUCCESS")) {
                try {
                    Scene scene = new Scene(
                            FXMLLoader.load(
                                    ClientApp.class.getResource("client-waiting-room.fxml")
                            )
                    );
                    Stage stage = (Stage) responseText.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.show();
                } catch (IOException e) {
                    System.out.println("Error at clientRegisterController: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
