package com.apcscs494.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    public static final int PORT = 8386;
    public static final int REGISTER = 0;
    public static final int ANSWER = 1;

    private static final ReentrantLock mutex = new ReentrantLock();

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int n = 10;
//        while (n <= 0) {
//            System.out.print("Enter number of players: ");
//            n = sc.nextInt();
//        }

        ServerSocket serverSocket = new ServerSocket(PORT, n);

        try {
            int i = 0;
            while (!serverSocket.isClosed()) {
                if (i < n) {
                    new Thread(new Player(serverSocket.accept(), n)).start();
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
