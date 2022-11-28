package com.apcscs494;

import java.io.Serializable;

class Packet implements Serializable {

  private String message;
  private int messageCode;

  public Packet(String message, int messageCode) {
    this.message = message;
    this.messageCode = messageCode;
  }

  public String getMessage() {
    return this.message;
  }

  public int getCode() {
    return this.messageCode;
  }

}