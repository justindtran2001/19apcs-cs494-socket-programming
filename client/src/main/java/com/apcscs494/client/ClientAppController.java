package com.apcscs494.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ClientAppController implements Initializable {
    @FXML
    AnchorPane rootPane;
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

    static Thread timerThread;
    static Integer remainingTime;
    static int timeAllowed = 15;
    static State currentState = State.WAITING;

    private static final ReentrantLock mutex = new ReentrantLock();

    static Client client;


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

        client.listenForGameResponse(
                rootPane,
                keywordLabel,
                hintLabel,
                guessCharTextField,
                guessKeywordTextField,
                serverResponseMessageLabel,
                scoreboardTableView
        );

//        this.setListenerForTimer();

        Platform.runLater(() -> rootPane.getScene().getWindow().setOnCloseRequest(windowEvent -> client.exit()));
    }

    public static void closeWindow(AnchorPane rootPane) {
        Platform.exit();
    }

    public void setListenerForTimer() {
        timerThread = new Thread(() -> {
            try {
                while (true) {
                    if (currentState == State.MY_TURN) {
                        try {
                            mutex.lock();
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
                            mutex.unlock();
                        } catch (Exception e) {
                            System.out.println("Error at countdown timer: " + e.getMessage());
                            throw e;
                        }
                    } else {
                        Platform.runLater(() -> timeRemainingLabel.setText(""));
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
            timerThread.stop();

            guessCharTextField.clear();
            guessKeywordTextField.clear();
            serverResponseMessageLabel.setText("");
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

    public static void setYourTurn(TextField guessCharTextField, TextField guessKeywordTextField, Label serverResponseMessageLabel) {
        try {
            currentState = State.MY_TURN;
            remainingTime = timeAllowed;

            timerThread = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        remainingTime--;
                        System.out.println("Remaining time: " + remainingTime);
                        Platform.runLater(() -> {
                            serverResponseMessageLabel.setText("You have " + remainingTime + " seconds to submit one answer.");
                            if (remainingTime <= 3)
                                serverResponseMessageLabel.setTextFill(Color.color(1.0, 0.0, 0.0));
                            else
                                serverResponseMessageLabel.setTextFill(Color.color(0, 0, 0));
                        });

                        if (remainingTime <= 0) {
                            client.sendToServer("(na),(na)");
                            Platform.runLater(() -> {
                                serverResponseMessageLabel.setTextFill(Color.color(0, 0, 0));
                                serverResponseMessageLabel.setText("");
                            });
                            break;
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    System.out.println("Error at countdown");
                    throw new RuntimeException(e);
                }
            });
            timerThread.start();

            Platform.runLater(() -> {
                guessCharTextField.setDisable(false);
                guessKeywordTextField.setDisable(false);
                serverResponseMessageLabel.setText("You have " + timeAllowed + " seconds to submit one answer.");
            });
        } catch (Exception e) {
            System.out.println("Error at set remainingTime for myTurn: " + e.getMessage());
            throw e;
        }
    }

    public static void disableTextFields(TextField guessCharTextField, TextField guessKeywordTextField) {
        if (currentState == State.WAITING) return;

        System.out.println("disableTextFields() called");
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
            try {
                scoreboardTableView.setItems(FXCollections.observableArrayList(gamePlayerData));
            } catch (Exception e) {
                System.out.println("Error at updating scoreboard: " + e.getMessage());
                throw e;
            }
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
