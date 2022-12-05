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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
    @FXML
    TableColumn<GamePlayerScore, Integer> rankCol;
    @FXML
    TableColumn<GamePlayerScore, String> usernameCol;
    @FXML
    TableColumn<GamePlayerScore, Integer> scoreCol;
    @FXML
    Label timeRemainingLabel;

    Thread timerThread;
    static Integer remainingTime;
    static State currentState = State.WAITING;

    private static final ReentrantLock mutex = new ReentrantLock();

    Client client;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }


        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

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

        this.setListenerForTimer();
    }

    public void setListenerForTimer() {
        timerThread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        mutex.lock();
                        if (currentState == State.MY_TURN) {
                            if (remainingTime > 0) {
                                remainingTime--;
                                TimeUnit.SECONDS.sleep(1);
                                System.out.println("Time remaining: " + remainingTime);

                                Platform.runLater(() -> timeRemainingLabel.setText("Time remaining: " + remainingTime + " seconds"));
                            } else {
                                sendEmptyAnswer();
                                currentState = State.WAITING;
//                                disableTextFields(guessCharTextField, guessKeywordTextField);
                                Platform.runLater(() -> timeRemainingLabel.setText(""));
                            }
                        }
                        else {
                            Platform.runLater(() -> timeRemainingLabel.setText(""));
                        }
                        mutex.unlock();
                    } catch (Exception e) {
                        System.out.println("Error at countdown timer: " + e.getMessage());
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        timerThread.start();
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
        try {
            mutex.lock();
            currentState = State.MY_TURN;
            remainingTime = 20;
            mutex.unlock();
        } catch (Exception e) {
            System.out.println("Error at set remainingTime for myTurn: " + e.getMessage());
            throw e;
        }
        Platform.runLater(() -> {
            guessCharTextField.setDisable(false);
            guessKeywordTextField.setDisable(false);
        });
    }

    public static void disableTextFields(TextField guessCharTextField, TextField guessKeywordTextField) {
        if (currentState == State.WAITING) return;

        currentState = State.WAITING;
        remainingTime = -1;
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

    private void sendEmptyAnswer() {
        try {
            client.sendToServer("(na),(na)");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(() -> {
            guessCharTextField.clear();
            guessKeywordTextField.clear();
        });
    }
}
