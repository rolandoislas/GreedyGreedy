package com.rolandoislas.greedygreedy.core.data;

import com.badlogic.gdx.graphics.Color;

import java.io.File;

/**
 * Created by rolando on 7/16/17.
 */
public class Constants {
    // Info
    public static final String NAME = "Greedy Greedy";
    public static final String VERSION = "1.0";
    // Path
    private static final File PATH_ROOT = new File(System.getProperty("user.home", ""), ".greedygreedy/");
    public static final File PATH_LOG = new File(PATH_ROOT, "log");
    // Color
    public static final Color COLOR_YELLOW = new Color(228/255f, 229/255f, 211/255f, 1);
    public static final Color COLOR_GREEN = new Color(219/255f, 229/255f, 211/255f, 1);
    public static final Color COLOR_GRAY = new Color(229/255f, 221/255f, 211/255f, 1);
    public static final Color COLOR_BLUE = new Color(211/255f, 224/255f, 229/255f, 1);
    public static final Color COLOR_RED = new Color(229/255f, 211/255f, 211/255f, 1);
    public static final Color COLOR_GREEN_DARK = new Color(22 / 255f, 117 / 255f, 42 / 255f, 1);
    public static final Color COLOR_WHITE = Color.WHITE;
    // Game
    public static final int MAX_PLAYERS = 6;
    // Preferences
    public static final String PREF_CATEGORY_GENERAL = "general";
    public static final String PREF_CATEGORY_SAVE = "save";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_GAMESTATE_SINGLE_PLAYER = "gamestateSinglePlayer";
    public static final String PREF_ACCESS_TOKEN = "accessToken";
    public static final String PREF_CODE_VERIFIER = "codeVerifier";
    public static final String PREF_REFRESH_TOKEN = "refreshToken";
    // Auth
    public static final int LOGIN_CALLBACK_PORT = 65177;
    public static final String AUTH0_DOMAIN = "https://greedygreedy.auth0.com/";
    public static final String AUTH0_AUDIENCE = "https://greedygreedy.herokuapp.com/";
    public static final String AUTH0_AUDIENCE_LOCAL = "http://localhost:5000/";
    public static final String AUTH0_CLIENT_ID = "qrWi5pnnayfCt66DMUdNHMeGqBWh3U9X";
    public static final String AUTH0_REDIRECT_URL = "https://greedygreedy.herokuapp.com/api/auth/callback";
    public static final String AUTH0_REDIRECT_URL_LOCAL = "http://localhost:5000/api/auth/callback";
    public static final String AUTH0_KEYSET = "https://greedygreedy.auth0.com/.well-known/jwks.json";
    public static final String AUTH0_SCOPE = "offline_access";
}
