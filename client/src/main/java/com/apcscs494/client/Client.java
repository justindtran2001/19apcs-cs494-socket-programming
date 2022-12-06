package com.apcscs494.client;

import com.apcscs494.client.constants.ResponseCode;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Client {
    private static Client instance;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    public static final String SERVER_HOST = "localhost";
    public static final int PORT = 1234;

    String username;

    // Response format: "message,responseType"
    // responseType in Response.java

    public static synchronized Client getInstance() throws IOException {
        try {
            if (instance == null) {
                // Connect to the server at localhost and defined port
                instance = new Client(new Socket(SERVER_HOST, PORT));
            }
            return instance;
        } catch (IOException e) {
            System.out.println("Error when opening a socket");
            e.printStackTrace();
            throw new IOException("Error when opening a socket");
        }
    }

    private Client(Socket socket) throws IOException {
        try {
            // Client constructor
            this.socket = socket;
            // writer for writing message to server
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // reader for reading message from server
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Client error at init: " + e.getMessage());
            e.printStackTrace();
            exit(socket, reader, writer);
        }
    }

    private void exit(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void exit() {
        try {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendToServer(String message) throws IOException {
        try {
            System.out.println("sendToServer(): " + message);
            // write message to writer
            writer.write(message);
            // start new line and flush for ending the message
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Client error at sendMessage: " + e.getMessage());
            e.printStackTrace();
            exit(socket, reader, writer);
            throw e;
        }
    }

    public void registerPlayerByUsername(String username) throws IOException {
        sendToServer(username);
        this.username = username;
    }

    public void listenForRegistrationConfirm(TextField usernameTextField, Text responseText) {
        new Thread(() -> {
            System.out.println("Waiting for registration...");
            if (socket.isConnected()) {
                try {
                    String receivedResponse = reader.readLine();
                    System.out.println("Response from server: " + receivedResponse);

                    if (receivedResponse.contains(ResponseCode.SUCCESS)) {
                        ClientRegisterController.switchToWaitingRoom(responseText);
                    } else if (receivedResponse.contains(ResponseCode.FAILED)) {
                        ClientRegisterController.rejectRegistration("Username already taken", usernameTextField,
                                responseText);
                    }
                } catch (Exception e) {
                    System.out.println("Client error at listenForRegistrationConfirm: " + e.getMessage());
                    e.printStackTrace();
                    exit(socket, reader, writer);
                }
            }
        }).start();
    }

    public void waitForGameStart(Pane rootPane) {
        new Thread(() -> {
            System.out.println("...Waiting for game start...");
            while (socket.isConnected()) {
                try {
                    String response = reader.readLine();
                    System.out.println("Response from server: " + response);
                    if (response.contains(ResponseCode.START_GAME)) {
                        ClientWaitingRoomController.startGame(rootPane);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Client error at waitForGameStart: " + e.getMessage());
                    e.printStackTrace();
                    exit(socket, reader, writer);
                    ClientAppController.closeWindow();
                    break;
                }
            }
        }).start();
    }

    public void listenForGameResponse(
            Label keywordLabel,
            Label hintLabel,
            TextField guessCharTextField,
            TextField guessKeywordTextField,
            Button submitButton,
            TextFlow serverResponseMessageTextFlow,
            TableView<GamePlayerScore> scoreboardTableView,
            Text winnerUsernameText) {

        // Handle response from server
        new Thread(() -> {
            System.out.println("Listening for game response...");
            while (socket.isConnected()) {
                try {
                    // read message from server
                    String receivedResponse = reader.readLine();
                    System.out.println("Response from game server: " + receivedResponse);
                    // process the message according to the message code
                    String[] resp = receivedResponse.split(",");
                    String code = resp[resp.length - 1];

                    switch (code) {
                        case ResponseCode.CURRENT_KEYWORD -> {
                            StringBuilder keywordAndHint = new StringBuilder(resp[0]);
                            for (int i = 1; i < resp.length - 1; ++i)
                                keywordAndHint.append(",").append(resp[i]);
                            String[] afterSplit = keywordAndHint.toString().split("-");
                            String keyword = afterSplit[0];
                            if (afterSplit.length > 1) {
                                StringBuilder hint = new StringBuilder(afterSplit[1]);
                                if (afterSplit.length > 2) {
                                    for (int i = 2; i < afterSplit.length; ++i)
                                        hint.append("-").append(afterSplit[i]);
                                }
                                System.out.println("Updating keyword: " + keyword);
                                ClientAppController.setKeyword(keyword, keywordLabel);

                                if (hint.length() > 0) {
                                    System.out.println("Updating hint: " + hint);
                                    ClientAppController.setHint(hint.toString(), hintLabel);
                                }
                            } else {
                                System.out.println("Updating keyword: " + keyword);
                                ClientAppController.setKeyword(keyword, keywordLabel);
                            }
                        }
                        case ResponseCode.YOUR_TURN -> {
                            System.out.println("Setting your turn");
                            ClientAppController.setYourTurn(guessCharTextField, guessKeywordTextField,
                                    serverResponseMessageTextFlow, submitButton);
                            continue;
                        }
                        case ResponseCode.END_GAME -> {
                            StringBuilder playerInfoCharSeq = new StringBuilder(resp[0]);
                            for (int i = 1; i < resp.length - 1; ++i)
                                playerInfoCharSeq.append(",").append(resp[i]);
                            StringBuilder displayMessage = new StringBuilder();
                            String winnerAnnouncement = "";

                            String[] playerInfoStringArr = playerInfoCharSeq.toString().split("##");
                            ArrayList<GamePlayerScore> gamePlayerData = new ArrayList<>();

                            for (String playerInfoString : playerInfoStringArr) {
                                String[] info = playerInfoString.split(",");
                                if (info.length != 5)
                                    throw new Exception("Invalid player info format");
                                GamePlayerScore aPlayerData = new GamePlayerScore(
                                        Long.parseLong(info[0]), // ID
                                        info[1], // Username
                                        Integer.parseInt(info[2]) // Score
                                );
                                gamePlayerData.add(aPlayerData);

                                if (Boolean.parseBoolean(info[4])) { // IsWinner
                                    if (Objects.equals(username, aPlayerData.username))
                                        winnerAnnouncement = "YOU'RE THE WINNER!\n";
                                    else
                                        winnerAnnouncement = aPlayerData.username + " is the winner.\n";
                                }
                            }
                            gamePlayerData.sort((o1, o2) -> o2.getScore() - o1.getScore());
                            for (int i = 0; i < gamePlayerData.size(); ++i) {
                                gamePlayerData.get(i).setRank(i + 1);
                            }

                            displayMessage.append("Wait 10 seconds for the next game.\n");

                            ClientAppController.setScoreboard(gamePlayerData, scoreboardTableView);
                            ClientAppController.setServerResponseMessage(displayMessage.toString(),
                                    serverResponseMessageTextFlow, winnerUsernameText);
                            ClientAppController.disableAnswerFunction(guessCharTextField, guessKeywordTextField,
                                    submitButton);
                            ClientAppController.setWinnerUsernameText(winnerAnnouncement, winnerUsernameText);
                            TimeUnit.SECONDS.sleep(10); // wait 10 seconds before starting new game
                            ClientAppController.setServerResponseMessage("", serverResponseMessageTextFlow, null);
                        }
                        case ResponseCode.RESULTS -> {
                            StringBuilder playerInfoCharSeq = new StringBuilder(resp[0]);
                            for (int i = 1; i < resp.length - 1; ++i)
                                playerInfoCharSeq.append(",").append(resp[i]);
                            String[] playerInfoStringArr = playerInfoCharSeq.toString().split("##");
                            ArrayList<GamePlayerScore> gamePlayerData = new ArrayList<>();
                            for (String playerInfoString : playerInfoStringArr) {
                                String[] info = playerInfoString.split(",");
                                if (info.length != 5)
                                    throw new Exception("Invalid player info format");
                                GamePlayerScore aPlayerData = new GamePlayerScore(
                                        Long.parseLong(info[0]), // ID
                                        info[1], // Username
                                        Integer.parseInt(info[2]) // Score
                                );
                                gamePlayerData.add(aPlayerData);
                            }
                            gamePlayerData.sort((o1, o2) -> o2.getScore() - o1.getScore());
                            for (int i = 0; i < gamePlayerData.size(); ++i) {
                                gamePlayerData.get(i).setRank(i + 1);
                            }
                            System.out.println("Updating scoreboard: " + gamePlayerData);
                            ClientAppController.setScoreboard(gamePlayerData, scoreboardTableView);
                            continue;
                        }
                        case ResponseCode.OUT_GAME -> {
                            System.out.println(
                                    ResponseCode.OUT_GAME + " received. " + "Closing sockets and application now.");
                            exit();
                            ClientAppController.closeWindow();
                            return;
                        }
                        case ResponseCode.LOST_TURN -> ClientAppController.setServerResponseMessage(
                                "You have lost your turn.\nWait for another players to finish.",
                                serverResponseMessageTextFlow,
                                null);
                    }
                    ClientAppController.disableAnswerFunction(guessCharTextField, guessKeywordTextField, submitButton);
                } catch (Exception e) {
                    System.out.println("Client error at listenForGameResponse: " + e.getMessage());
                    e.printStackTrace();
                    exit(socket, reader, writer);
                    return;
                }
            }
        }).start();
    }
}
