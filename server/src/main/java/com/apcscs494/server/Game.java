package com.apcscs494.server;

import com.apcscs494.server.constants.GameState;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/* message structure 

// CHARACTER,KEYWORD

// example1: "P,PYTHON"
// example2: "P,"
// example3: ",PYTHON"
// example4: ","

*/

class Game {
    public Integer total_turn = 0;

    public GameState state;

    // map <player_id, number of turn played>
    public HashMap<Player, Integer> playerNumOfTurns;

    // map <id, player>
    public LinkedHashMap<Long, Integer> playerIDScoreList = new LinkedHashMap<>();
    public Long nextAvailableID = 0L;

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

    public Long register() {
        Long newID = this.nextAvailableID;
        this.nextAvailableID++;

        return newID;
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
                playerNumOfTurns.entrySet()) {
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

        if (total_turn > 2 && currentQuestion.guessTheKeyword(answer[1])) {
            this.end(currentPlayerId);
            // add 5 points to current player
            playerIDScoreList.put(currentPlayerId, playerIDScoreList.get(currentPlayerId) + 5);
            return;
        }

        if (currentQuestion.guessACharacter(answer[0].charAt(0))) {
            // add 1 point to current player
            playerIDScoreList.put(currentPlayerId, playerIDScoreList.get(currentPlayerId) + 1);
        }
    }

}