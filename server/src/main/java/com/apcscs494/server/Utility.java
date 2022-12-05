package com.apcscs494.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    public static String convertResultsToString(ArrayList<GamePlayerData> results) {
        String str = "";
        for (GamePlayerData data : results) {
            str += String.format("%s##", data.toString());
        }

        return str;
    }

    public static ArrayList<GamePlayerData> convertStringToResults(String results) {
        String[] arr = results.split("##");
        ArrayList<GamePlayerData> resultArr = new ArrayList<>();

        for (String s : arr) {
            resultArr.add(new GamePlayerData(s));
        }

        return resultArr;
    }

    public static boolean hasEnoughPlayers(HashMap<Long, Player> players) {
        return players.size() >= Server.MIN_PLAYER && players.size() <= Server.MAX_PLAYER;
    }

    public static boolean usedName(String name, HashMap<Long, Player> players) {
        for (Map.Entry<Long, Player> entry : players.entrySet()) {
            if (entry.getValue().username.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
