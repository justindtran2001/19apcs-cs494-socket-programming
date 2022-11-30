package com.apcscs494.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientAppController implements Initializable {
    @FXML
    TextField guessCharTextField;
    @FXML
    TextField guessKeywordTextField;
    @FXML
    Label responseLabel;
    @FXML
    Button submitButton;

    Client client;

    public static void setResponse(String receivedResponse, Label responseLabel) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                responseLabel.setText(receivedResponse);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }

        // Listening from server
        client.receiveResponseFromServer(responseLabel);


        // Set event handlers from UI elements
        guessCharTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    sendAnswerToServer();
                }
            }
        });
        guessKeywordTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    sendAnswerToServer();
                }
            }
        });
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendAnswerToServer();
            }
        });
    }

    public void sendAnswerToServer() {
        try {
            String guessChar = guessCharTextField.getText();
            String guessKeyword = guessKeywordTextField.getText();
            if (guessChar.isEmpty()) return;

            String answer = guessChar + "," + guessKeyword;

            client.sendToServer(answer);

            guessCharTextField.clear();
            guessKeywordTextField.clear();
        } catch (Exception e) {
            System.out.println("Client error at register: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
