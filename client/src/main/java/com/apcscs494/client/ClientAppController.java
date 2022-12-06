package com.apcscs494.client;

import com.apcscs494.client.constants.ClientAppWindowState;
import com.apcscs494.client.constants.TimerType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

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
    TextFlow serverResponseMessageTextFlow;
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
    Text usernameText;

    static Thread timerThread;
    static Integer remainingTime;
    static int timeAllowed = 15;
    static State currentState = State.WAITING;

    static ClientAppWindowState clientAppWindowState = ClientAppWindowState.OPENED;

    static Client client;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = Client.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Client.");
        }

        usernameText.setText(client.username);

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
                keywordLabel,
                hintLabel,
                guessCharTextField,
                guessKeywordTextField,
                submitButton,
                serverResponseMessageTextFlow,
                scoreboardTableView
        );

        Platform.runLater(() -> rootPane.getScene().getWindow().setOnCloseRequest(windowEvent -> {
            clientAppWindowState = ClientAppWindowState.CLOSED;
            client.exit();
        }));
    }

    public static void closeWindow() {
        Platform.exit();
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
            serverResponseMessageTextFlow.getChildren().clear();
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

    public static void setYourTurn(TextField guessCharTextField, TextField guessKeywordTextField, TextFlow serverResponseMessageTextFlow, Button submitButton) {
        try {
            currentState = State.MY_TURN;

            startTimer(timeAllowed, TimerType.YOUR_TURN, serverResponseMessageTextFlow);

            Platform.runLater(() -> {
                guessCharTextField.setDisable(false);
                guessKeywordTextField.setDisable(false);
                submitButton.setDisable(false);
            });
        } catch (Exception e) {
            System.out.println("Error at set remainingTime for myTurn: " + e.getMessage());
            throw e;
        }
    }

    public static void disableAnswerFunction(TextField guessCharTextField, TextField guessKeywordTextField, Button submitButton) {
        if (currentState == State.WAITING) return;

        System.out.println("disableAnswerFunction() called");
        currentState = State.WAITING;
        remainingTime = -1;
        Platform.runLater(() -> {
            guessCharTextField.setDisable(true);
            guessKeywordTextField.setDisable(true);
            submitButton.setDisable(true);
        });
    }

    public static void setServerResponseMessage(String message, TextFlow serverResponseMessageTextFlow) {
        Platform.runLater(() -> {
            if (message.contains("Wait 10 seconds")) {
                Text winnerText = new Text(message.split("\n")[0]);
                Text waitText = new Text(message.split("\n")[1]);
                serverResponseMessageTextFlow.getChildren().clear();
                Separator separator = new Separator(Orientation.HORIZONTAL);
                separator.prefWidthProperty().set(serverResponseMessageTextFlow.getWidth());
                serverResponseMessageTextFlow.getChildren().addAll(winnerText, waitText);
                startTimer(10, TimerType.WAIT_FOR_NEW_GAME, serverResponseMessageTextFlow);
            } else {
                Text text = new Text(message);
                text.setFont(Font.font(16.0));
                serverResponseMessageTextFlow.getChildren().clear();
                serverResponseMessageTextFlow.getChildren().add(text);
            }
        });
    }

    private static void startTimer(int seconds, TimerType type, TextFlow serverResponseMessageTextFlow) {
        remainingTime = seconds;
        timerThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(1000);
                    remainingTime--;
                    System.out.println("Remaining time: " + remainingTime);
                    Platform.runLater(() -> {
                        serverResponseMessageTextFlow.getChildren().clear();
                        Text text = new Text();
                        text.setFont(Font.font(16.0));

                        if (type == TimerType.YOUR_TURN) {
                            text.setText("You have " + remainingTime + " seconds to submit one answer.");
                        } else if (type == TimerType.WAIT_FOR_NEW_GAME) {
                            text.setText("A new game will be started in " + remainingTime + " seconds.");
                        }

                        if (remainingTime <= 3)
                            text.setFill(Color.color(1.0, 0.0, 0.0));
                        else
                            text.setFill(Color.color(0, 0, 0));
                        serverResponseMessageTextFlow.getChildren().add(text);
                    });



                    if (remainingTime <= 0) {
                        client.sendToServer("(na),(na)");
                        Platform.runLater(() -> serverResponseMessageTextFlow.getChildren().clear());
                        break;
                    }
                    if (clientAppWindowState == ClientAppWindowState.CLOSED) break;
                }
            } catch (InterruptedException | IOException e) {
                System.out.println("Error at countdown");
                throw new RuntimeException(e);
            }
        });
        timerThread.start();
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
}
