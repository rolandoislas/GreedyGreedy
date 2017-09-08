package com.rolandoislas.greedygreedy.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.auth.AuthenticationHandlerHosted;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.ArgumentParser;
import com.rolandoislas.greedygreedy.desktop.util.AchievementHandler;
import com.rolandoislas.greedygreedy.desktop.util.AdHandler;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 500;
		config.height = 720;
		config.title = Constants.NAME;

		// Icon
		if (System.getProperties().getProperty("os.name").toLowerCase().contains("mac")) {
			config.addIcon("image/icon_512.png", Files.FileType.Internal);
			config.addIcon("image/icon_256.png", Files.FileType.Internal);
		}
		config.addIcon("image/icon_32.png", Files.FileType.Internal);
		config.addIcon("image/icon_16.png", Files.FileType.Internal);

		new LwjglApplication(new GreedyClient(new ArgumentParser(arg), new AchievementHandler(),
				new AuthenticationHandlerHosted(), new AdHandler()), config);
	}
}
