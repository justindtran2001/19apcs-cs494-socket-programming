package com.apcscs494.client;

import javafx.scene.control.Label;

import java.io.*;
import java.net.Socket;

public class Client {
    private static Client instance;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

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

    public void receiveResponseFromServer(Label responseLabel) {
        Socket socket = this.socket;
        new Thread(() -> {
            System.out.println("...Listening...");
            while (socket.isConnected()) {
                try {
                    String receivedResponse = reader.readLine();
                    ClientAppController.setResponse(receivedResponse, responseLabel);
                } catch (Exception e) {
                    System.out.println("Client error at listen: " + e.getMessage());
                    exit(socket, reader, writer);
                }
            }
        }).start();
    }

    public void sendToServer(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Client error at sendMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
