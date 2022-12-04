package com.apcscs494.client;

public class GamePlayerScore {
    Long id;
    String username;
    Integer score;

    public GamePlayerScore() {
    }

    public GamePlayerScore(Long id, String username, Integer score) {
        this.id = id;
        this.username = username;
        this.score = score;
    }
}
