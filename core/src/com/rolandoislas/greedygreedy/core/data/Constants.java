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
}
