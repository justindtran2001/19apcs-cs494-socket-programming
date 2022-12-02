package com.apcscs494.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.apcscs494.server.constants.GameState;
import com.apcscs494.server.constants.Response;

// A handler for a player/client
class Player implements Runnable {

    private static HashMap<Long, Player> players = new HashMap<>();
    private static Player admin = null;
    private static final Game game;

    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    private String username = null;
    private Long id = null;

    static {
        try {
            game = new Game();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return this.username;
    }

    public Player(Socket socket) throws IOException {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // When a user joined, read the username first
            this.username = reader.readLine();

            if (this.username == ClientAdmin.ADMIN_PHRASE) {
                admin = this;
            } else {
                if (players.size() < Server.MAX_PLAYER) {
                    Long register_id = game.register(this.username);
                    this.id = register_id;
                    players.put(register_id, this);
                } else {
                    writer.write("ROOM IS FULL");
                    writer.newLine();
                    writer.flush();
                    exit();
                }
            }

            if (admin != null) {
                if (Utility.hasEnoughPlayers(players)) {
                    admin.writer.write(ClientAdmin.CAN_START);
                    admin.writer.newLine();
                    admin.writer.flush();
                } else {
                    admin.writer.write(ClientAdmin.CAN_NOT_START);
                    admin.writer.newLine();
                    admin.writer.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("Client error at init: " + e.getMessage());
            e.printStackTrace();
            writer.write("FAILED");
            writer.newLine();
            writer.flush();
        }
    }

    @Override
    public void run() {
        String message;
        while (this.socket.isConnected()) {
            try {
                // receives message from client
                message = reader.readLine();
                if (Utility.isFromAdmin(message)) {
                    AdminHandler(message);
                } else {
                    ClientHandler(message);
                }
            } catch (IOException e) {
                this.exit();
                break;
            }
        }

    }

    private void RestartGameHandler() {
        game.restart();
        broadcastTo(game.getNextPlayerId(), "", Response.YOUR_TURN);
    }

    private void ClientHandler(String message) {
        if (game.getState() == GameState.FORCE_END) {
            return;
        }

        Game.GAME_RESPONSE code = game.process(message);

        if (code == Game.GAME_RESPONSE.CONTINUE) {
            // continue guessing
            broadcastTo(id, "", Response.YOUR_TURN);
        } else if (code == Game.GAME_RESPONSE.NEXTPLAYER) {
            // wrong answer - lost turn;
            broadcastTo(id, "", Response.LOST_TURN);
            broadcastTo(game.getNextPlayerId(), "", Response.YOUR_TURN);
        } else {
            // Game is over
            ArrayList<GamePlayerData> results = game.getResults();
            if (results != null)
                broadcastAll(Utility.convertResultsToString(results), Response.END_GAME);
            else
                broadcastAll("No results", Response.END_GAME);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            } finally {
                RestartGameHandler();
            }
        }
    }

    private void AdminHandler(String message) {
        if (message.contains("START") && Utility.hasEnoughPlayers(players)) {
            RestartGameHandler();
        } else {
            game.endByAdmin();
            broadcastAll("Game ended by Admin", Response.OUT_GAME);
        }
    }

    public void broadcastAll(String message, String code) {
        players.forEach((id, player) -> {
            try {
                player.writer.write(message + "," + code);
                player.writer.newLine();
                player.writer.flush();
                if (code == Response.OUT_GAME) {
                    players.remove(id);
                    exit();
                }
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
                exit();
            }
        });
    }

    public void broadcastTo(Long id, String message, String code) {
        try {
            if (players.containsKey(id)) {
                Player player = players.get(id);
                player.writer.write(message + "," + code);
                player.writer.newLine();
                player.writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Handler exception at broadcastT: " + e.getMessage());
            exit();
        }
    }

    private void exit() {
        try {
            this.writer.close();
            this.reader.close();
            this.socket.close();
        } catch (Exception e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
        }
    }
}

// We tried using serialized object but wasn't able to solve the bug
/*
 *
 * private Socket socket;
 * private ObjectInputStream in;
 * private ObjectOutputStream out;
 * public String userName;
 *
 *
 * public void register() {
 * try {
 * this.socket.get
 * Packet recv = (Packet) this.in.readObject();
 * this.userName = recv.getMessage();
 * } catch (Exception e) {
 * System.out.println("Register player failed!");
 * e.printStackTrace();
 * }
 * }
 *
 */