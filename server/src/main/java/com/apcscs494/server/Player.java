package com.apcscs494.server;

import com.apcscs494.server.constants.GameState;
import com.apcscs494.server.constants.Response;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

// A handler for a player/client
class Player implements Runnable {

    static HashMap<Long, Player> players = new HashMap<>();
    static ArrayList<Player> pendingPlayers = new ArrayList<>();
    static final Game game;

    public Socket socket = null;
    public BufferedWriter writer = null;
    public BufferedReader reader = null;
    public String username = null;
    public Long id = null;

    static {
        try {
            game = new Game();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Player(Socket socket) throws IOException {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = null;

            registerHandler();

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
                if (this.username != null)
                    ClientHandler(message);

            } catch (IOException e) {
                this.exit();
                break;
            }
        }

    }

    private void registerHandler() throws IOException {
        String message;

        while (this.socket.isConnected()) {
            try {
                message = reader.readLine();
                if (!Utility.usedName(message, players)) {
                    this.username = message;
                    writer.write("SUCCESS");
                    writer.newLine();
                    writer.flush();
                    break;
                } else {
                    writer.write("FAILED");
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                this.exit();
                break;
            }
        }

        System.out.println("Player " + username + " has joined.");

        if (players.size() < Server.MAX_PLAYER) {
            if (game.getState() == GameState.RUNNING) {
                pendingPlayers.add(this);
            } else {
                Long register_id = game.register(this.username);
                this.id = register_id;
                players.put(register_id, this);
            }

        }
    }

    private void restartGameHandler() {
        System.out.println("restartGameHandler() called");
        game.restart();

        for (Player p : pendingPlayers) {
            Long register_id = game.register(p.username);
            p.id = register_id;
            players.put(register_id, p);
            pendingPlayers.remove(p);
        }

        broadcastAll(game.getCurrentKeyWordState() + "-" + game.getCurrentQuestion().getHint(),
                Response.CURRENT_KEYWORD);
        broadcastTo(game.getNextPlayerId(false), "", Response.YOUR_TURN);
    }

    private void ClientHandler(String message) {
        if (game.getState() == GameState.FORCE_END) {
            return;
        }

        if (message == null)
            return;

        Game.RESPONSE code = game.process(message);

        if (code == Game.RESPONSE.CONTINUE) {
            // continue guessing
            broadcastAll(game.getCurrentKeyWordState(), Response.CURRENT_KEYWORD);
            broadcastTo(id, "", Response.YOUR_TURN);
        } else if (code == Game.RESPONSE.NEXT_PLAYER) {
            // wrong answer - lost turn;
            broadcastTo(id, "", Response.LOST_TURN);

            Long nextPlayerId = game.getNextPlayerId(false);
            while (game.isDisqualified(nextPlayerId)) {
                nextPlayerId = game.getNextPlayerId(true);
            }
            System.out.println("nextPlayerId: " + nextPlayerId);
            broadcastTo(nextPlayerId, "", Response.YOUR_TURN);
        } else {
            // Game is over => Restart game
            broadcastAll(Utility.convertResultsToString(game.getResults()), Response.END_GAME);
            restartGameHandler();
            return;
        }

        broadcastAll(Utility.convertResultsToString(game.getResults()), Response.RESULTS);
    }

    public void broadcastAll(String message, String code) {
        players.forEach((id, player) -> {
            try {
                player.writer.write(message + "," + code);
                player.writer.newLine();
                player.writer.flush();
                if (code == Response.OUT_GAME) {
                    game.state = GameState.INITIAL;
                    players.remove(id);
                    game.playerList.Remove(id, player.username);
                    // player.exit();
                }
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
                player.exit();
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

    void exit() {
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