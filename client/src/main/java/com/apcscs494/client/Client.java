package com.apcscs494.client;

import com.apcscs494.client.constants.ResponseCode;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

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
    String username;

    // Response format: "message,responseType"
    // responseType in Response.java

    public static synchronized Client getInstance() throws IOException {
        try {
            if (instance == null) {
                instance = new Client(new Socket("localhost", 8386));
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
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Client error at init: " + e.getMessage());
            e.printStackTrace();
            exit(socket, reader, writer);
        }
    }

    private void exit(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendToServer(String message) throws IOException {
        try {
            writer.write(message);
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

    public void listenForRegistrationConfirm(Text responseText) {
        new Thread(() -> {
            System.out.println("Waiting for registration...");
            while (socket.isConnected()) {
                try {
                    String receivedResponse = reader.readLine();
                    System.out.println("Response from server: " + receivedResponse);

                    ClientRegisterController.handleResponse(receivedResponse, responseText);
                    break;
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
                    if (response.contains("START")) {
                        ClientWaitingRoomController.handleResponse(response, rootPane);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Client error at waitForGameStart: " + e.getMessage());
                    e.printStackTrace();
                    exit(socket, reader, writer);
                }
            }
        }).start();
    }

    public void listenForGameResponse(
            Label keywordLabel,
            Label hintLabel,
            TextField guessCharTextField,
            TextField guessKeywordTextField,
            Label serverResponseMessageLabel,
            TableView<GamePlayerScore> scoreboardTableView
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    System.out.println("Listening for game response...");
                    while (socket.isConnected()) {
                        try {
                            String receivedResponse = reader.readLine();
                            System.out.println("Response from game server: " + receivedResponse);

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
                                        ClientAppController.setKeyword(keyword, keywordLabel);

                                        if (hint.length() > 0)
                                            ClientAppController.setHint(hint.toString(), hintLabel);
                                    } else
                                        ClientAppController.setKeyword(keyword, keywordLabel);
                                }
                                case ResponseCode.YOUR_TURN -> {
                                    ClientAppController.setYourTurn(guessCharTextField, guessKeywordTextField);
                                    continue;
                                }
                                case ResponseCode.END_GAME -> {
                                    StringBuilder playerInfoCharSeq = new StringBuilder(resp[0]);
                                    for (int i = 1; i < resp.length - 1; ++i)
                                        playerInfoCharSeq.append(",").append(resp[i]);
                                    StringBuilder displayMessage = new StringBuilder();
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
                                                displayMessage.append("CONGRATULATION! YOU'RE THE WINNER!\n");
                                            else
                                                displayMessage.append("Player ").append(aPlayerData.username).append(" is the winner.");
                                        }
                                    }
                                    ClientAppController.setScoreboard(gamePlayerData, scoreboardTableView);
//                                String scoreboard = playerInfoCharSeq.toString().replace("##", "\n");
                                    ClientAppController.setServerResponseMessage(displayMessage.toString(), serverResponseMessageLabel);
                                    TimeUnit.SECONDS.sleep(10); // wait 10 seconds before starting new game
                                }
                                case ResponseCode.OUT_GAME -> {
                                    exit(socket, reader, writer);
                                    // TODO: Exit application
                                    Platform.exit();
                                    return;
                                }
                            }
                            ClientAppController.disableTextFields(guessCharTextField, guessKeywordTextField);
                        } catch (Exception e) {
                            System.out.println("Client error at listenForGameResponse: " + e.getMessage());
                            e.printStackTrace();
                            exit(socket, reader, writer);
                            return;
                        }
                    }
                }
            }
        }).start();
    }
}
