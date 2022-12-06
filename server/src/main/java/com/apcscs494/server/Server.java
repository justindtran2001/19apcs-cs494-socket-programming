package com.apcscs494.server;

import com.apcscs494.server.constants.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.ReentrantLock;

class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 1234;
    public static final int REGISTER = 0;
    public static final int ANSWER = 1;
    public static final int MAX_PLAYER = 10;
    public static final int MIN_PLAYER = 2;
    public static final String CAN_START = "CAN_START";
    public static final String CAN_NOT_START = "CAN_NOT_START";

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;

    int numOfPlayers = 0;


    private static final ReentrantLock mutex = new ReentrantLock();

    public Server(ServerSocket serverSocket) throws IOException {
        try {
            this.serverSocket = serverSocket;
//            serverSocket.setSoTimeout(10000);
            this.socket = new Socket(HOST, PORT);
            this.socket = serverSocket.accept();
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (SocketTimeoutException e) {
            System.out.println("ServerSocket.accept() is timed out");
            e.printStackTrace();
            exit(socket, reader, writer);
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error at Server constructor: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public void exit() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            if (!Player.players.isEmpty())
                Player.players.forEach((id, player) -> {
                    player.exit();
                });
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exit(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            if (this.serverSocket != null) this.serverSocket.close();
            if (!Player.players.isEmpty())
                Player.players.forEach((id, player) -> {
                    player.exit();
                });
        } catch (IOException e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void acceptingPlayers() throws IOException {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    if (numOfPlayers < MAX_PLAYER) {
                        new Thread(new Player(serverSocket.accept())).start();
                        System.out.println("Player " + numOfPlayers + " joined");
                        try {
                            mutex.lock();
                            numOfPlayers = numOfPlayers + 1;
                            mutex.unlock();
                        } catch (Exception e) {
                            System.out.println("Error at counting players: " + e.getMessage());
                            throw e;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error at acceptingPlayers: " + e.getMessage());
                e.printStackTrace();
                exit(socket, reader, writer);
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
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
                    break;
                }
            }
        }).start();
    }

    public void notifyEndGame() {
        Player.players.forEach((id, player) -> {
            try {
                player.writer.write("Game ended by Admin" + "," + Response.OUT_GAME);
                player.writer.newLine();
                player.writer.flush();

                Player.players.remove(id);
                try {
                    mutex.lock();
                    numOfPlayers = numOfPlayers - 1;
                    mutex.unlock();
                } catch (Exception e) {
                    System.out.println("Error at removing players: " + e.getMessage());
                    throw e;
                }
                player.exit();
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
                player.exit();
            }
        });
    }

    void notifyNextPlayer() {
        Long nextPlayerId = Player.game.getNextPlayerId(false);
        try {
            if (Player.players.containsKey(nextPlayerId)) {
                Player player = Player.players.get(nextPlayerId);
                player.writer.write("" + "," + Response.YOUR_TURN);
                player.writer.newLine();
                player.writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Handler exception at broadcastT: " + e.getMessage());
            exit();
        }
    }

    public void broadcastAllToStartGame() {
        Player.players.forEach((id, player) -> {
            try {
                player.writer.write("Game started by Admin" + "," + Response.START_GAME);
                player.writer.newLine();
                player.writer.flush();
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
            }
        });
    }

    public void sendQuestion() {
        Player.players.forEach((id, player) -> {
            try {
                player.writer.write(
                        Player.game.getCurrentKeyWordState() + "-" +
                                Player.game.getCurrentQuestion().getHint() + "," + Response.CURRENT_KEYWORD);
                player.writer.newLine();
                player.writer.flush();
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
            }
        });
    }

    public void chooseNextPlayer() {
        try {
            Long id = Player.game.getNextPlayerId(false);
            if (Player.players.containsKey(id)) {
                Player player = Player.players.get(id);
                player.writer.write("" + "," + Response.YOUR_TURN);
                player.writer.newLine();
                player.writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Handler exception at broadcastT: " + e.getMessage());
            exit();
        }
    }

    public void sendScoreboard() {
        Player.players.forEach((id, player) -> {
            try {
                player.writer.write(Utility.convertResultsToString(Player.game.getResults()) + "," + Response.RESULTS);
                player.writer.newLine();
                player.writer.flush();
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
            }
        });
    }
}