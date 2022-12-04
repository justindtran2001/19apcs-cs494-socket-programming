package com.apcscs494.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import com.apcscs494.server.constants.GameState;

/* message structure 
// CHARACTER,KEYWORD
// example1: "P,PYTHON"
// example2: "P,"
// example3: ",PYTHON"
// example4: ","
*/

class Game {
    public enum RESPONSE {
        CONTINUE,
        NEXT_PLAYER,
        END,
    }

    public Integer total_turn = 0;

    public GameState state;

    public GamePlayerList playerList = new GamePlayerList();
    public Long nextAvailableID = 0L;

    public final ArrayList<Question> questionList = new ArrayList<>();
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

            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error when loading database.txt", e);
        }
    }

    private Question getNewQuestion() {
        boolean hasUnusedQuestion = false;
        for (Question q : questionList) {
            if (!q.used) {
                hasUnusedQuestion = true;
                break;
            }
        }
        if (!hasUnusedQuestion) {
            System.out.println("Out of questions");
            return null;
        }
        try {
            Question q = questionList.get(new Random().nextInt(questionList.size()));

            if (q.used)
                return getNewQuestion();

            q.used = true;
            return q;
        }
        catch (Exception e) {
            System.out.println("Error at getNewQuestion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
        total_turn = 0;
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
        if (winnerId == -1)
            return null;
        this.restart();
        return results;
    }

    public boolean forceEndGame() {
        return playerList.HasForceEndCondition();
    }

    public RESPONSE process(String message) {
        total_turn += 1;
        GamePlayerData playerData = playerList.GetCurrent();
        if (message.equals("(na),(na)")) {
            playerData.AddTurn();
            playerList.MoveNext();
            return RESPONSE.NEXT_PLAYER;
        }

        String[] answer = message.split(",");

        if (this.forceEndGame()) {
            return RESPONSE.END;
        }

        // "(string), (string)"
        if (answer.length > 1)
            if (total_turn > 2 && currentQuestion.guessTheKeyword(answer[1])) {
                playerData.AddPoint(5);
                playerData.SetKeyWordWinner();
                winnerId = playerData.id;
                return RESPONSE.END;
            }

        if (currentQuestion.guessACharacter(answer[0].charAt(0))) {
            playerData.AddPoint(1);
            return RESPONSE.CONTINUE;
        } else {
            playerData.AddTurn();
            playerList.MoveNext();
            return RESPONSE.NEXT_PLAYER;
        }
    }

    public Long getNextPlayerId() {
        return playerList.GetCurrent().id;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }
}