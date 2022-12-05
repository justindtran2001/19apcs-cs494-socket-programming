package com.apcscs494.client;

public class GamePlayerScore {
    Integer rank;
    Long id;
    String username;
    Integer score;

    public GamePlayerScore() {
    }

    public GamePlayerScore(Integer rank, Long id, String username, Integer score) {
        this.rank = rank;
        this.id = id;
        this.username = username;
        this.score = score;
    }

    public GamePlayerScore(Long id, String username, Integer score) {
        this.id = id;
        this.username = username;
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
