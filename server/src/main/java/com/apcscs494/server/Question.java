package com.apcscs494.server;

import java.util.ArrayList;

public class Question {
    final String keyword;
    final String hint;
    ArrayList<Character> guessedCharList;
    boolean used = false;

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
        if (!this.keyword.contains(guessChar.toString()) || guessedCharList.contains(guessChar))
            return false;

        this.guessedCharList.add(guessChar);
        return true;
    }

    public boolean guessTheKeyword(String guessKeyword) {
        System.out.println("Question.guessTheKeyword()");
        System.out.println(keyword);
        System.out.println(guessKeyword);
        if (this.keyword.equalsIgnoreCase(guessKeyword)) {
            for (char c : keyword.toCharArray()) {
                this.guessedCharList.add(c);
            }
            return true;
        }

        return false;
    }

    public Integer getLength() {
        return keyword.length();
    }

    public String getHint() {
        return hint;
    }

    @Override
    public String toString() {
        return "Question{" +
                "keyword='" + keyword + '\'' +
                ", hint='" + hint + '\'' +
                '}';
    }
}
