package com.apcscs494.server;

import java.util.ArrayList;
import java.util.Set;

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

  public void Remove(Long id, String username) {
    if (head == null) return;
    if (head.id == id && head.username == username) {
      if (head == tail) {
        head = null;
        tail = null;
        current = null;
      } else {
        if (current == head) current = current.next;
        head = head.next;
      }
    } else if (head.next == null) {
      return;
    } else {
      GamePlayerData cur = head;
      do {
        if (cur.next.id == id && cur.next.username == username) {
          cur.next = cur.next.next;
          break;
        }
        cur = cur.next;
      } while (cur != head);
    }
  }

  public void Reset() {
    GamePlayerData cur = head;
    if (head != null) {
      do {
        cur.isKeyWordWinner = false;
        cur.point = 0;
        cur.turn = 0;
        cur = cur.next;
      } while (cur != head);
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

  public boolean IsAllDisqualified(Set<Long> set) {
    GamePlayerData cur = head;
    if (head != null) {
      do {
        if (!set.contains(cur.id))
          return false;
        cur = cur.next;
      } while (cur != head);
    }
    return true;
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