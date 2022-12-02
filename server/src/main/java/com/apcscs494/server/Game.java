package com.apcscs494.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.apcscs494.server.constants.GameState;

/* message structure 
// CHARACTER,KEYWORD
// example1: "P,PYTHON"
// example2: "P,"
// example3: ",PYTHON"
// example4: ","
*/

class Game {
    public static enum GAME_RESPONSE {
        CONTINUE,
        NEXTPLAYER,
        END,
    };

    public Integer total_turn = 0;

    public GameState state;

    public GamePlayerList playerList = new GamePlayerList();
    public Long nextAvailableID = 0L;

    public ArrayList<Question> questionList = new ArrayList<>();
    public Question currentQuestion;

    public Long winnerId = -1L;

    public Game() throws Exception {
        // read questions from database here
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream("database.txt");
            if (is == null)
                throw new Exception("inputStream is null");
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

            currentQuestion = this.getNewQuestion();

            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error when loading database.txt", e);
        }
    }

    private Question getNewQuestion() {
        Question q = questionList.get((int) Math.random() * questionList.size());

        if (q.used)
            return getNewQuestion();

        return q;
    }

    public String getCurrentKeyWordState() {
        return currentQuestion.getCurrentKeyword();
    }

    public Long register(String username) {
        Long newID = nextAvailableID;
        playerList.Add(newID, username);
        nextAvailableID++;
        return newID;
    }

    public GameState getState() {
        return state;
    }

    public void restart() {
        // reset everything
        state = GameState.RUNNING;
        winnerId = -1L;
        currentQuestion = this.getNewQuestion();
        playerList.Reset();
    }

    public void endByAdmin() {
        state = GameState.FORCE_END;
    }

    // public void start() {
    // // state = GameState.RUNNING;
    // }

    public ArrayList<GamePlayerData> getResults() {
        ArrayList<GamePlayerData> results = playerList.ToList();
        this.restart();
        if (winnerId == -1)
            return null;
        return results;
    }

    public boolean forceEndGame() {
        return playerList.HasForceEndCondition();
    }

    public GAME_RESPONSE process(String message) {
        total_turn += 1;
        String[] answer = message.split(",");
        GamePlayerData playerData = playerList.GetCurrent();

        if (this.forceEndGame()) {
            return GAME_RESPONSE.END;
        }

        if (total_turn > 2 && currentQuestion.guessTheKeyword(answer[1])) {
            playerData.AddPoint(5);
            playerData.SetKeyWordWinner();
            winnerId = playerData.id;
            return GAME_RESPONSE.END;
        }

        if (currentQuestion.guessACharacter(answer[0].charAt(0))) {
            playerData.AddPoint(1);
            return GAME_RESPONSE.CONTINUE;
        } else {
            playerData.AddTurn();
            playerList.MoveNext();
            return GAME_RESPONSE.NEXTPLAYER;
        }
    }

    public Long getNextPlayerId() {
        return playerList.GetCurrent().id;
    }
}