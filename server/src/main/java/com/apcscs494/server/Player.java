package com.apcscs494.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

import com.apcscs494.server.constants.Response;

// A handler for a player/client
class Player implements Runnable {

    private static HashMap<Long, Player> players = new HashMap<>();
    private static Player admin = null;
    private static final Game game;

    static {
        try {
            game = new Game();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    private String username = null;
    private Long id = null;

    public String getUsername() {
        return this.username;
    }

    public Player(Socket socket,) throws IOException {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // When a user joined, read the username first
            this.username = reader.readLine();

            if (this.username == ClientAdmin.ADMIN_PHRASE) {
                admin = this;
            } else {
                if (players.size() < 10) {
                    Long register_id = game.register(this.username);
                    this.id = register_id;
                    players.put(register_id, this);
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

    public void broadcast(String message, String code) {
        //game logic
        //game state broadcasting
        players.forEach((id, player) -> {
            try {
                player.writer.write(message + "," + code);
                player.writer.newLine();
                player.writer.flush();
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

    @Override
    public void run() {
        String message;

        while (this.socket.isConnected()) {
            try {

                // receives answer
                message = reader.readLine();
                // admin-thisisadmin-SHUTDOWN
                // TODO
                
                
                // else 
                Game.GAME_RESPONSE code = game.process(message);

                if (code == Game.GAME_RESPONSE.CONTINUE) {
                    // continue guessing
                    broadcastTo(id, "", Response.YOUR_TURN);
                } else if (code == Game.GAME_RESPONSE.NEXTPLAYER) {
                    // wrong answer - lost turn;
                    broadcastTo(id, "", Response.LOST_TURN);
                    broadcastTo(game.GetNextPlayerId(), "", Response.YOUR_TURN);
                } else {
                    
                    game.restart();
                    // broadcast(, );
                }


            } catch (IOException e) {
                this.exit();
                break;
            }
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