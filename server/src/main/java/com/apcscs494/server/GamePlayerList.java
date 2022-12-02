package com.apcscs494.server;

import java.util.ArrayList;

public class GamePlayerList {
  // Represents the node of list.

  public GamePlayerData head = null;
  public GamePlayerData tail = null;
  public GamePlayerData current = null;

  public void Add(Long id, String username) {
    GamePlayerData newPlayer = new GamePlayerData(id, username);

    if (head == null) {
      head = newPlayer;
      current = head;
      tail = newPlayer;
      newPlayer.next = head;
    } else {
      tail.next = newPlayer;
      tail = newPlayer;
      tail.next = head;
    }
  }

  public GamePlayerData GetNext() {
    GamePlayerData data = current.next;
    current = current.next;
    return data;
  }

  public GamePlayerData GetCurrent() {
    return current;
  }

  public void MoveNext() {
    current = current.next;
  }

  public boolean HasForceEndCondition() {
    GamePlayerData cur = head;
    if (head != null) {
      do {
        if (cur.turn >= 5)
          return true;
        cur = cur.next;
      } while (cur != head);

    }
    return false;
  }

  public ArrayList<GamePlayerData> ToList() {
    GamePlayerData cur = head;
    ArrayList<GamePlayerData> result = new ArrayList<GamePlayerData>();
    if (head != null) {
      do {
        result.add(cur);
        cur = cur.next;
      } while (cur != head);
    }
    return result;
  }
}