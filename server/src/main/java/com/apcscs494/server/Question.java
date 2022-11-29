package com.apcscs494.server;

import java.util.ArrayList;

public class Question {
    final String keyword;
    final String hint;
    ArrayList<Character> guessedCharList;

    public Question(String keyword, String hint) {
        this.keyword = keyword;
        this.hint = hint;
        guessedCharList = new ArrayList<>();
    }

    public String getCurrentKeyword() {
        String res = this.keyword;
        for (int i = 0; i < this.keyword.length(); ++i) {
            if (res.charAt(i) != '*' && !guessedCharList.contains(res.charAt(i))) {
                res = res.replace(res.charAt(i), '*');
            }
        }
        return res;
    }

    public boolean guessACharacter(Character guessChar) {
        if (!this.keyword.contains(guessChar.toString()))
            return false;

        this.guessedCharList.add(guessChar);
        return true;
    }

    public boolean guessTheKeyword(String guessKeyword) {
        return this.keyword.equals(guessKeyword);
    }

    @Override
    public String toString() {
        return "Question{" +
                "keyword='" + keyword + '\'' +
                ", hint='" + hint + '\'' +
                '}';
    }
}