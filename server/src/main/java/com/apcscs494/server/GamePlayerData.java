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
    return this.id.toString() + "," + this.username + "," + this.point.toString();
  }
}