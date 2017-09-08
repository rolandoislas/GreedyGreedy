package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import org.jrenner.smartfont.SmartFontGenerator;

import java.util.HashMap;

/**
 * Created by Rolando on 2/13/2017.
 */
public class TextUtil {
	private static HashMap<Integer, BitmapFont> fonts = new HashMap<Integer, BitmapFont>();
	private static SmartFontGenerator generator;

	public static BitmapFont generateScaledFont(float scale) {
		if (generator == null) {
			generator = new SmartFontGenerator();
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.color = Color.WHITE;
			parameter.borderColor = Color.DARK_GRAY;
			parameter.borderWidth = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? 1 : 5;
			generator.setParameter(parameter);
		}
		int size = (int) (50f * Gdx.graphics.getHeight() * scale / 720f);
		if (fonts.containsKey(size))
			return fonts.get(size);
		Logger.debug("Loading font: scale %f, stage %s", scale, GreedyClient.getStage().toString());
		FileHandle fontFile = Gdx.files.internal("font/collvetica.ttf");
		BitmapFont font =  generator.createFont(fontFile, "collvetica", size);
		fonts.put(size, font);
		return font;
	}

	public static void dispose() {
		for (HashMap.Entry<Integer, BitmapFont> font : fonts.entrySet())
			font.getValue().dispose();
		fonts.clear();
		generator = null;
	}
}
