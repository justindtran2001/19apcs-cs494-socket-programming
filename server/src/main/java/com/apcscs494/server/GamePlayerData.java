package com.apcscs494.server;

public class GamePlayerData {
    public Long id;
    public String username;
    public Integer point;
    public Integer turn;
    public boolean isKeyWordWinner;
    public GamePlayerData next;

    public GamePlayerData(Long id, String username) {
        this.isKeyWordWinner = false;
        this.username = username;
        this.id = id;
        this.point = 0;
        this.turn = 0;
    }

    public GamePlayerData(String src) {
        try {
            String[] arr = src.split(",");
            this.id = Long.parseLong(arr[0]);
            this.username = arr[1];
            this.point = Integer.parseInt(arr[2]);
            this.turn = Integer.parseInt(arr[3]);
            this.isKeyWordWinner = Boolean.parseBoolean(arr[4]);
        } catch (Exception e) {
            System.out.println("Error at GamePlayerData constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void AddPoint(Integer point) {
        this.point += point;
    }

    public void AddTurn() {
        this.turn += 1;
    }

    public void SetKeyWordWinner() {
        this.isKeyWordWinner = true;
    }

    public String toString() {
        String isWinner = "false";
        if (this.isKeyWordWinner) {
            isWinner = "true";
        }
        return String.format("%d,%s,%d,%d,%s", id, username, point, turn, isWinner);
    }
}