package com.apcscs494.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.apcscs494.server.constants.AdminCode;
import com.google.gson.Gson;

public class Utility {
    static Gson gson = new Gson();

    public static String convertResultsToString(ArrayList<GamePlayerData> results) {

        return gson.toJson(results);
    }

    public static ArrayList<GamePlayerData> convertStringToResults(String results) {

        return gson.fromJson(results, ArrayList.class);
    }

    public static boolean isFromAdmin(String message) {
        return message.contains(AdminCode.ADMIN_PHRASE);
    }

    public static boolean hasEnoughPlayers(HashMap<Long, Player> players) {
        return players.size() >= Server.MIN_PLAYER && players.size() <= Server.MAX_PLAYER;
    }

}
