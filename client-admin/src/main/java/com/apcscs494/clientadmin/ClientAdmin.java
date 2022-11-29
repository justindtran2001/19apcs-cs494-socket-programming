package com.apcscs494.clientadmin;

import com.apcscs494.server.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientAdmin {
    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    private String username = null;
    private Scanner sc = null;

    public ClientAdmin(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.sc = new Scanner(System.in);
            this.register();
        } catch (Exception e) {
            System.out.println("Client error at init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void register() {

        System.out.print("Enter your username: ");
        this.username = sc.nextLine();

        try {
            writer.write(this.username);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Client error at register: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            while (socket.isConnected()) {
                writer.write(this.username + ": " + this.sc.nextLine());
                writer.newLine();
                writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Client error at sendMessage: " + e.getMessage());
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
                    System.out.println("Client error at listen: " + e.getMessage());
                    exit();
                }
            }
        }).start();
    }

    private void exit() {
        try {
            this.sc.close();
            this.writer.close();
            this.reader.close();
            this.socket.close();
        } catch (Exception e) {
            System.out.println("Handler exception at exit: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", Server.PORT);
            ClientAdmin client = new ClientAdmin(socket);
            client.listenMessage();
            client.sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}