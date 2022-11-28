package com.apcscs494;

import com.apcscs494.constants.GameState;

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
    public HashMap<Long, Player> playerIDList;

    // map <keyword, hint>
    public HashMap<String, String> keywordList;
    public ArrayList<Question> questionList;

    // determine the turn of player id x
    public Long currentPlayerId;

    // current question
    public Question currentQuestion;

 

    public Game() {
        // read questions from database here
        
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