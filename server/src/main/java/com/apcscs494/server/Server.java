package com.apcscs494.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

class Server {
    public static final int PORT = 8386;
    public static final int REGISTER = 0;
    public static final int ANSWER = 1;
    public static final int MAX_PLAYER = 10;
    public static final int MIN_PLAYER = 2;
    public static final String ADMIN_PHRASE = "admin-thisisadmin";
    public static final String CAN_START = "CAN_START";
    public static final String CAN_NOT_START = "CAN_NOT_START";

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;


    private static final ReentrantLock mutex = new ReentrantLock();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        new Thread(() -> {
            try {
                this.socket = this.serverSocket.accept();
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public Server() {
        try {
            this.serverSocket = new ServerSocket(8386, 10);
            this.socket = serverSocket.accept();
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Server error at init: " + e.getMessage());
            e.printStackTrace();
//            exit(socket, reader, writer);
        }
    }

    private void exit(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void acceptingPlayers() throws IOException {
        new Thread(() -> {
            try {
                int i = 0;
                while (!serverSocket.isClosed()) {
                    if (i < MAX_PLAYER) {
                        new Thread(new Player(serverSocket.accept())).start();
                        System.out.println("Player " + i + " joined");
                        try {
                            mutex.lock();
                            i = i + 1;
                            mutex.unlock();
                        } catch (Exception e) {
                            System.out.println("Error at counting players: " + e.getMessage());
                            throw e;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                exit(socket, reader, writer);
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            while (socket.isConnected()) {
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Server error at sendMessage: " + e.getMessage());
        }
    }

    public void listenMessage() {
        Socket theSocket = this.socket;
        new Thread(() -> {
            System.out.println("...Listening...");
            while (theSocket.isConnected()) {
                try {
                    System.out.println(reader.readLine());
                } catch (Exception e) {
                    System.out.println("Server error at listenMessage: " + e.getMessage());
                    exit(socket, reader, writer);
                }
            }
        }).start();
    }
}