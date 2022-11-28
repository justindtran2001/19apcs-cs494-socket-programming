package com.apcscs494;

import com.apcscs494.constants.GameState;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/* message structure 

// CHARACTER,KEYWORD

// example1: "P,PYTHON"
// example2: "P,"
// example3: ",PYTHON"
// example4: ","

*/

class Game {
    public Integer total_turn;

    public GameState state;

    // map <player_id, number of turn played>
    public HashMap<Player, Integer> playerList;

    // map <id, player>
    public HashMap<Long, Player> playerIDList = new HashMap<>();


    // map <keyword, hint>
    public ArrayList<Question> questionList = new ArrayList<>();

    // determine the turn of player id x
    public Long currentPlayerId;

    // current question
    public Question currentQuestion;

    public Game() throws Exception {
        // read questions from database here
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream("database.txt");
            if (is == null) throw new Exception("inputStream is null");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            int len = Integer.parseInt(reader.readLine());
            System.out.println("\nReading " + len + " pairs of keyword and hint:");
            for (int i = 0; i < len; ++i) {
                String keyword = reader.readLine();
                String hint = reader.readLine();
                Question question = new Question(keyword, hint);
                questionList.add(question);
                System.out.println("Keyword: " + keyword);
                System.out.println("Hint: " + hint);
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error when loading database.txt", e);
        }
    }

    public String getCurrentKeyWordState() {
        // "*a*b*b"
        return currentQuestion.getCurrentKeyword();
    }

    public boolean register(Long id) {
        return playerIDList.containsKey(id);
    }

    public GameState getState() {
        return this.state;
    }

    public void restart() {
        this.state = GameState.RUNNING;

        // reset point to 0
        // reset everything
    }

    public void start() {
        this.state = GameState.RUNNING;
    }

    public void end(Long winnerId) {
        this.state = GameState.END;
    }

    public boolean forceEndGame() {

        for (HashMap.Entry<Player, Integer> player :
                playerList.entrySet()) {
            if (player.getValue() == 5) {
                return false;
            }
        }
        return true;
    }

    public void process(String message) {
        this.total_turn += 1;
        String[] answer = message.split(",");

        if (this.forceEndGame()) {
            this.end((long) -1);
        }

        if (total_turn > 2 && Objects.equals(currentQuestion.keyword, answer[1])) {
            this.end(currentPlayerId);
            // add 5 points to current player
            return;
        }

        if (currentQuestion.keyword.contains(answer[0])) {
            // add 1 point to current player
        }


    }

}