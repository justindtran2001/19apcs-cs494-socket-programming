package com.apcscs494.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
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
    @FXML
    VBox scoreboardVBox;
    @FXML
    TableView<GamePlayerScore> scoreboardTableView;

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

        TableColumn<GamePlayerScore, Long> idCol = new TableColumn<>("ID");
        idCol.setMinWidth(100);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<GamePlayerScore, Long> usernameCol = new TableColumn<>("Username");
        usernameCol.setMinWidth(100);
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<GamePlayerScore, Long> scoreCol = new TableColumn<>("Score");
        scoreCol.setMinWidth(100);
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        scoreboardTableView.getColumns().addAll(idCol, usernameCol, scoreCol);

        // Set event handlers from UI elements
        guessCharTextField.setDisable(true);
        guessCharTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                sendAnswerToServer();
            }
        });
        guessKeywordTextField.setDisable(true);
        guessKeywordTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                sendAnswerToServer();
            }
        });
        submitButton.setOnAction(event -> sendAnswerToServer());

        client.listenForGameResponse(keywordLabel, hintLabel, guessCharTextField, guessKeywordTextField, serverResponseMessageLabel, scoreboardTableView);
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
        Platform.runLater(() -> keywordLabel.setText("Keyword: " + keyword));
    }

    public static void setHint(String hint, Label hintLabel) {
        Platform.runLater(() -> hintLabel.setText("Hint: " + hint));
    }

    public static void setYourTurn(TextField guessCharTextField, TextField guessKeywordTextField) {
        currentState = State.MY_TURN;
        Platform.runLater(() -> {
            guessCharTextField.setDisable(false);
            guessKeywordTextField.setDisable(false);
        });
    }

    public static void disableTextFields(TextField guessCharTextField, TextField guessKeywordTextField) {
        if (currentState == State.WAITING) return;

        currentState = State.WAITING;
        Platform.runLater(() -> {
            guessCharTextField.setDisable(true);
            guessKeywordTextField.setDisable(true);
        });
    }

    public static void setServerResponseMessage(String scoreboard, Label serverResponseMessageLabel) {
        Platform.runLater(() -> serverResponseMessageLabel.setText(scoreboard));
    }

    public static void setScoreboard(ArrayList<GamePlayerScore> gamePlayerData, TableView<GamePlayerScore> scoreboardTableView) {
        Platform.runLater(() -> {
            final ObservableList<GamePlayerScore> data = FXCollections.observableArrayList(gamePlayerData);

            scoreboardTableView.setItems(data);
        });
    }
}
