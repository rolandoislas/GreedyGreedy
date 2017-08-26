package com.rolandoislas.greedygreedy.core;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.auth.AuthenticationHandler;
import com.rolandoislas.greedygreedy.core.stage.Stage;
import com.rolandoislas.greedygreedy.core.stage.StageLoad;
import com.rolandoislas.greedygreedy.core.util.*;

import java.util.logging.Level;

public class GreedyClient extends ApplicationAdapter {
	public static ArgumentParser args;
	private static Stage stage;
	public static AchievementHandler achievementHandler;
	public static AuthenticationHandler authenticationHandler;

    public GreedyClient(ArgumentParser argumentParser, AchievementHandler achievementHandler,
						AuthenticationHandler authenticationHandler) {
		GreedyClient.args = argumentParser;
		GreedyClient.achievementHandler = achievementHandler;
		GreedyClient.authenticationHandler = authenticationHandler;
		// Init logger
		if (GreedyClient.args.logDebug)
			Logger.setLevel(Level.FINE);
		else if (GreedyClient.args.logExtra)
			Logger.setLevel(Level.FINER);
		else if (GreedyClient.args.logVerbose)
			Logger.setLevel(Level.FINEST);
		Logger.info("Starting %s version %s", Constants.NAME, Constants.VERSION);
	}

	public static Stage getStage() {
		return stage;
	}

	@Override
	public void create () {
		stage = new StageLoad(true);
	}

	@Override
	public void render () {
		super.render();
		Color color = getStage().getBackgroundColor();
		Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.draw();
		stage.act();
	}

	@Override
	public void dispose () {
		super.dispose();
		stage.dispose();
	}

	@Override
	public void pause() {
		super.pause();
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			setStage(new StageLoad(false));
			TextUtil.dispose();
		}
	}

	@Override
	public void resume() {
		super.resume();
		if (Gdx.app.getType() == Application.ApplicationType.Android)
			create();
	}

	public static void setStage(Stage stage) {
		Logger.debug("Setting stage to %s", stage.getClass().getSimpleName());
		try {
			if (GreedyClient.getStage() != null)
				GreedyClient.getStage().dispose();
		}
		catch (IllegalArgumentException e) {
			Logger.exception(e);
		}
		GreedyClient.stage = stage;
		Gdx.input.setInputProcessor(stage);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		GreedyClient.stage.resize(width, height);
	}
}
