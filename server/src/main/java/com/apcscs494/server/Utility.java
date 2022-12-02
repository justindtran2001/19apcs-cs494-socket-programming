package com.apcscs494.server;

import java.util.ArrayList;
import com.google.gson.Gson;


public class Utility {
    static Gson gson = new Gson();
    
    public static String convertResultsToString(ArrayList<GamePlayerData> results) {

        return gson.toJson(results);
    }

    public static ArrayList<GamePlayerData> convertStringToResults(String results) {

        return gson.fromJson(results, ArrayList.class);
    }

}
