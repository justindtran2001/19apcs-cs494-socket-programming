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
    public static final int MAX_PLAYER = 10;
    public static final int MIN_PLAYER = 2;

    private static ReentrantLock mutex = new ReentrantLock();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT, MAX_PLAYER);
        new Thread(new Player(serverSocket.accept())).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                try {
                    socket = new Socket("localhost", PORT);
                    ClientAdmin admin = new ClientAdmin(socket);
                    admin.listenMessage();
                    admin.sendMessage();
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