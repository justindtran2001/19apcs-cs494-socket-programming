package com.apcscs494.client;

import com.apcscs494.client.constants.ResponseCode;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.*;
import java.net.Socket;

public class Client {
    private static Client instance;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

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

    public void listenForGameResponse(Text keywordLabel, Text hintLabel, TextField guessCharTextField, TextField guessKeywordTextField) {
        new Thread(() -> {
            System.out.println("Waiting for question...");
            while (socket.isConnected()) {
                try {
                    String receivedResponse = reader.readLine();
                    System.out.println("Response from game server: " + receivedResponse);

                    String[] resp = receivedResponse.split(",");
                    String code = resp[resp.length - 1];

                    if (code.equals(ResponseCode.CURRENT_KEYWORD)) {
                        StringBuilder keywordAndHint = new StringBuilder(resp[0]);
                        for (int i = 1; i < resp.length - 1; ++i)
                            keywordAndHint.append(", ").append(resp[i]);

                        String[] afterSplit = keywordAndHint.toString().split("-");
                        ClientAppController.setKeyword(keywordAndHint.toString().split("-")[0], keywordLabel);

                        if (afterSplit.length > 1)
                            ClientAppController.setHint(keywordAndHint.toString().split("-")[1], hintLabel);
                    } else if (code.equals(ResponseCode.YOUR_TURN)) {
                        ClientAppController.setYourTurn(guessCharTextField, guessKeywordTextField);
                    } else if (code.equals(ResponseCode.LOST_TURN)) {
                        ClientAppController.setLostTurn(guessCharTextField, guessKeywordTextField);
                    } else if (code.equals(ResponseCode.END_GAME)) {
                        // TODO: Show scoreboard

                    } else if (code.equals(ResponseCode.OUT_GAME)) {
                        exit(socket, reader, writer);
                        // TODO: Exit application
                        return;
                    }

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
