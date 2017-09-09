package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.pay.Transaction;
import com.google.gson.*;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;

import java.util.ArrayList;

/**
 * Created by rolando on 5/26/17.
 */
public class PreferencesUtil {
    private static final String PREFERENCED_PREFIX = "com.rolandoislas.greedygreedy.config." +
            (GreedyClient.args.preferencesProfile.isEmpty() ? "default" :
                    GreedyClient.args.preferencesProfile
                            .replace(".", "")
                            .replace(" ", "_")
                            .toLowerCase())
            + ".";

    public static Preferences get(String name) {
        if (name.startsWith("."))
            name = name.replace(".", "");
        return Gdx.app.getPreferences(PREFERENCED_PREFIX + name);
    }

    public static String getPlayerName() {
        String noName = "Best Player Ever";
        JsonObject userInfo;
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        try {
             userInfo = new Gson().fromJson(preferences.getString(Constants.PREF_USER_INFO), JsonObject.class);
        }
        catch (JsonSyntaxException e) {
            requestAndSaveUserInfo();
            return noName;
        }
        if (userInfo == null) {
            requestAndSaveUserInfo();
            return noName;
        }
        if (userInfo.has("nickname") && userInfo.get("nickname").getAsString().length() >= 3)
            return userInfo.get("nickname").getAsString();
        else if (userInfo.get("sub").getAsString().startsWith("google"))
            return "A Google User";
        else if (userInfo.get("sub").getAsString().startsWith("facebook"))
            return "A Facebook User";
        else {
            requestAndSaveUserInfo();
            return noName;
        }
    }

    private static void requestAndSaveUserInfo() {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        try {
            preferences.putString(Constants.PREF_USER_INFO, GreedyApi.getUserInfo());
            preferences.flush();
        } catch (GreedyException ignore) {}
    }

    public static String getToken() {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        return preferences.contains(Constants.PREF_ACCESS_TOKEN) ?
                preferences.getString(Constants.PREF_ACCESS_TOKEN)  : "";
    }

    public static int getPoints() {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        return preferences.contains(Constants.PREF_POINTS) ? preferences.getInteger(Constants.PREF_POINTS) : 0;
    }

    public static void addPoints(int points) {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        preferences.putInteger(Constants.PREF_POINTS, getPoints() + points);
        preferences.flush();
    }

    public static void updatePointsAsync() {
        Thread updatePointsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int points = GreedyApi.getPoints();
                    PreferencesUtil.setPoints(points);
                } catch (GreedyException e) {
                    Logger.exception(e);
                }
            }
        });
        updatePointsThread.setName("Update Points");
        updatePointsThread.setDaemon(true);
        updatePointsThread.start();
    }

    public static void setPoints(int points) {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        preferences.putInteger(Constants.PREF_POINTS, points);
        preferences.flush();
    }

    public static Icon getIcon() {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        int iconOrdinal = preferences.getInteger(Constants.PREF_ICON, Icon.DIE_FIVE.ordinal());
        if (iconOrdinal >= Icon.values().length || iconOrdinal < 0)
            return Icon.DIE_FIVE;
        return Icon.values()[iconOrdinal];
    }

    public static void setIcon(Icon icon) {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        preferences.putInteger(Constants.PREF_ICON, icon.ordinal());
        preferences.flush();
    }

    public static void saveFailedTransaction(Transaction transaction) {
        Gson gson = new Gson();
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        JsonArray transactions = gson.fromJson(preferences.getString(Constants.PREF_FAILED_TRANSACTIONS),
                JsonArray.class);
        if (transactions == null)
            transactions = new JsonArray();
        transactions.add(gson.toJson(transaction));
        preferences.putString(Constants.PREF_FAILED_TRANSACTIONS, transactions.toString());
        preferences.flush();
    }

    public static ArrayList<Transaction> getFailedTransactions() {
        ArrayList<Transaction> transactionsList = new ArrayList<Transaction>();
        Gson gson = new Gson();
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        JsonArray transactions = gson.fromJson(preferences.getString(Constants.PREF_FAILED_TRANSACTIONS),
                JsonArray.class);
        if (transactions == null)
            return transactionsList;
        preferences.remove(Constants.PREF_FAILED_TRANSACTIONS);
        for (JsonElement transactionElement : transactions)
            transactionsList.add(gson.fromJson(transactionElement.getAsString(), Transaction.class));
        preferences.flush();
        return transactionsList;
    }
}
