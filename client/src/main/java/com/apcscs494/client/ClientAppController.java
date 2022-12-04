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
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientAppController implements Initializable {
    @FXML
    TextField guessCharTextField;
    @FXML
    TextField guessKeywordTextField;
    @FXML
    Label keywordLabel;
    @FXML
    Label hintLabel;
    @FXML
    Button submitButton;
    @FXML
    Label serverResponseMessageLabel;

    // TODO: Text for response detail message

    static State currentState = State.WAITING;

    Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }

        // Listening from server
//        client.receiveResponseFromServer(responseLabel);


        // Set event handlers from UI elements
        guessCharTextField.setDisable(true);
        guessCharTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    sendAnswerToServer();
                }
            }
        });
        guessKeywordTextField.setDisable(true);
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

        client.listenForGameResponse(keywordLabel, hintLabel, guessCharTextField, guessKeywordTextField, serverResponseMessageLabel);
    }

    public void sendAnswerToServer() {
        if (currentState == State.WAITING) return;

        try {
            String guessChar = guessCharTextField.getText();
            String guessKeyword = guessKeywordTextField.getText();
            if (guessChar.isEmpty() && guessKeyword.isEmpty()) {
                System.out.println("Submit answer: " + "(na),(na)");
                client.sendToServer("(na),(na)");
                guessCharTextField.clear();
                guessKeywordTextField.clear();
                return;
            }

            if (guessChar.isEmpty())
                guessChar = "(na)";

            String answer = guessChar + "," + guessKeyword;
            System.out.println("Submit answer: " + answer);

            client.sendToServer(answer);

            guessCharTextField.clear();
            guessKeywordTextField.clear();
        } catch (Exception e) {
            System.out.println("Client error at sendAnswerToServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setKeyword(String keyword, Label keywordLabel) {
        Platform.runLater(() -> {
            keywordLabel.setText("Keyword: " + keyword);
        });
    }

    public static void setHint(String hint, Label hintLabel) {
        Platform.runLater(() -> {
            hintLabel.setText("Hint: " + hint);
        });
    }

    public static void setYourTurn(TextField guessCharTextField, TextField guessKeywordTextField) {
        currentState = State.MY_TURN;
        Platform.runLater(() -> {
            guessCharTextField.setDisable(false);
            guessKeywordTextField.setDisable(false);
        });
    }

    public static void setLostTurn(TextField guessCharTextField, TextField guessKeywordTextField) {
        currentState = State.WAITING;
        Platform.runLater(() -> {
            guessCharTextField.setDisable(true);
            guessKeywordTextField.setDisable(true);
        });
    }
}
