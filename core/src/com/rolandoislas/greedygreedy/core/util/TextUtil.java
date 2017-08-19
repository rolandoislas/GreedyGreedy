package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Rolando on 2/13/2017.
 */
public class TextUtil {
	private static HashMap<Integer, BitmapFont> fonts = new HashMap<Integer, BitmapFont>();

	public static BitmapFont generateScaledFont(float scale) {
		int size = (int) (50f * Gdx.graphics.getHeight() * scale / 720f);
		if (fonts.containsKey(size))
			return fonts.get(size);
		Logger.extra("Generating font scale %f", scale);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/coolvetica.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = size;
		parameter.color = Color.WHITE;
		parameter.borderColor = Color.DARK_GRAY;
		parameter.borderWidth = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? 1 : 5;
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		fonts.put(size, font);
		return font;
	}

	public static void dispose() {
		fonts.clear();
	}
}
