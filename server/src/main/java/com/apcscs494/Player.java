package com.apcscs494;

import com.apcscs494.constants.GameState;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

// A handler for a player/client
class Player implements Runnable {
    private static ArrayList<Player> players = new ArrayList<>();
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

    public Player(Socket socket, int maxPlayer) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = reader.readLine();
            //

            players.add(this);

            do  {
                this.id = new Random().nextLong();
            }
            while (!game.register(this.id));

            if (players.size() == maxPlayer) {
                game.start();
            }

            broadcast("Player " + username + " joined", Server.REGISTER);
        } catch (Exception e) {
            System.out.println("Client error at init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcast(String message, int code) {
        //game logic
        //game state broadcasting
        for (Player p : players) {
            try {
                if (this.username != null && !p.username.equals(this.username)) {
                    p.writer.write(message);
                    p.writer.newLine();
                    p.writer.flush();
                }
            } catch (Exception e) {
                System.out.println("Handler exception at broadcast: " + e.getMessage());
                exit();
            }
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
                message = reader.readLine();
                // this.broadcast(message, Server.REGISTER);

                if (game.getState() != GameState.END) {
                    // broadcast game hint and current keyword so far
                    game.process(message);
                } else {
                    // broadcast end game data first then restart
                    game.restart();
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