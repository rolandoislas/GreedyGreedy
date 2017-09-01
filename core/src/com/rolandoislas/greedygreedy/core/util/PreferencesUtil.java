package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;

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
}
