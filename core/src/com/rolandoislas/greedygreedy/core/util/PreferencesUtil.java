package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Created by rolando on 5/26/17.
 */
public class PreferencesUtil {
    private static final String PREFERENCED_PREFIX = "com.rolandoislas.greedygreedy.config.";

    public static Preferences get(String name) {
        if (name.startsWith("."))
            name = name.replace(".", "");
        return Gdx.app.getPreferences(PREFERENCED_PREFIX + name);
    }
}