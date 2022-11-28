package com.apcscs494;

import java.util.ArrayList;

public class Question {
    String keyword;
    String hint;

    ArrayList<Character> guessedCharList;

    public String getCurrentKeyword() {
        String res = new String(keyword);
        for (Character c : guessedCharList) {
            res.replace(c, '*');
        }
        return res;
    }

}
