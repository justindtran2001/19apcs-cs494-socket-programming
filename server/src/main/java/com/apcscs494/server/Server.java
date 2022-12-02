package com.apcscs494.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

class Server {
    public static final int PORT = 8386;
    public static final int REGISTER = 0;
    public static final int ANSWER = 1;

    private static ReentrantLock mutex = new ReentrantLock();

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int n = 0;
        while (n <= 0) {
            System.out.print("Enter number of players: ");
            n = sc.nextInt();
        }

        ServerSocket serverSocket = new ServerSocket(PORT, n);
        new Thread(new Player(serverSocket.accept())).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                try {
                    socket = new Socket("localhost", PORT);
                    ClientAdmin client = new ClientAdmin(socket);
                    client.listenMessage();
                    client.sendMessage();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            int i = 0;
            while (!serverSocket.isClosed()) {
                if (i < n) {
                    new Thread(new Player(serverSocket.accept())).start();
                    System.out.println("Player " + i + " joined");
                    try {
                        mutex.lock();
                        i = i + 1;
                        mutex.unlock();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            serverSocket.close();
        }
        sc.close();
    }
}