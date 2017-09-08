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
    public static final int API_VERSION = 1;
    public static String GITHUB_REPO = "https://github.com/rolandoislas/GreedyGreedy";
    public static final boolean FORCE_DEV_API = true;
    public static final String FORCE_API_URL = ""; // Dev option to specify a different server
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
    public static long TURN_TIME = 60 * 1000;
    public static int[] WINNING_POINTS_DISTRIBUTIONS = new int[]{50, 25, 10, 1, 1, 1};
    // Preferences
    public static final String PREF_CATEGORY_GENERAL = "general";
    public static final String PREF_CATEGORY_SAVE = "save";
    public static final String PREF_GAMESTATE_SINGLE_PLAYER = "gamestateSinglePlayer";
    public static final String PREF_ACCESS_TOKEN = "accessToken";
    public static final String PREF_CODE_VERIFIER = "codeVerifier";
    public static final String PREF_REFRESH_TOKEN = "refreshToken";
    public static final String PREF_USER_INFO = "userInfo";
    public static final String PREF_POINTS = "points";
    public static final String PREF_ICON = "icon";
    public static final String PREF_FAILED_TRANSACTIONS = "failedTransactions";
    // Auth
    public static final int LOGIN_CALLBACK_PORT = 65177;
    public static final String AUTH0_DOMAIN = "https://greedygreedy.auth0.com/";
    public static final String AUTH0_AUDIENCE = "https://greedygreedy.herokuapp.com/";
    public static final String AUTH0_AUDIENCE_LOCAL = "http://localhost:5000/";
    public static final String AUTH0_AUDIENCE_DEV = "https://greedygreedy-dev.herokuapp.com/";
    public static final String AUTH0_CLIENT_ID = "qrWi5pnnayfCt66DMUdNHMeGqBWh3U9X";
    public static final String AUTH0_KEYSET = "https://greedygreedy.auth0.com/.well-known/jwks.json";
    public static final String AUTH0_SCOPE = "openid profile offline_access";
    // Command
    public static final int COMMAND_REGISTER = 0;
    public static final int COMMAND_CLICK_DIE = 1;
    public static final int COMMAND_CLICK_ROLL = 2;
    public static final int COMMAND_CLICK_STOP = 3;
    public static final int COMMAND_WHOAMI = 4;
    public static final int COMMAND_PLAYER_UPDATE = 5;
    public static final int COMMAND_DICE_UPDATE = 6;
    public static final int COMMAND_TURN_END = 7;
    public static final int COMMAND_ACTION_FAILED = 8;
    public static final int COMMAND_ACTIVE_POINTS = 9;
    public static final int COMMAND_GAME_END = 10;
    public static final int COMMAND_LAST_ROUND = 11;
    public static final int COMMAND_ZILCH_WARNING = 12;
    public static final int COMMAND_ACHIEVEMENT = 13;
    public static final int COMMAND_COUNTDOWN = 14;
    public static final int COMMAND_ROLL_SUCCESS = 15;
    // Billing
    public static final String BILLING_KEY_GOOGLE = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhqfgqXOP0FV3IFV+KbQK" +
            "8qbzXM2bCdhDUnvULS4+cb8u5tJErqsN/w60FyW+PFelTGGuFqAjdPfvVTp9pcc/NKxCaWDT/aRlx5jBbTn+dIBC/ILk8vDbK8RRowv/" +
            "dL3EHxGH54KUM9FMP8BBfJJRE46Ej0Yjguvl9ghTC0FxWBBkzskoblByUVTsWO6kPQTw+iPmumq+/nE4Sc/CRA5eKFogZLkaicNsXp4q" +
            "hkk7DYZARYgbzj0JCD+uQXB41dhB0LRk5Bi0IoO9d6u9mJmSHP8qhRujML0jRqIgsamrWeQeH8bHUtesn1hwEsWGoy6YDRUUpRSh5fR/" +
            "2pdMSe7LJQIDAQAB";
}
