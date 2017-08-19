package com.rolandoislas.greedygreedy.core;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.stage.Stage;
import com.rolandoislas.greedygreedy.core.stage.StageLoad;
import com.rolandoislas.greedygreedy.core.util.AchievementHandler;
import com.rolandoislas.greedygreedy.core.util.ArgumentParser;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

public class GreedyClient extends ApplicationAdapter {
	public static ArgumentParser args;
	private static Stage stage;
	public static AchievementHandler achievementHandler;

    public GreedyClient(ArgumentParser argumentParser, AchievementHandler achievementHandler) {
		GreedyClient.args = argumentParser;
		GreedyClient.achievementHandler = achievementHandler;
		Logger.info("Starting %s version %s", Constants.NAME, Constants.VERSION);
	}

	public GreedyClient(AchievementHandler achievementHandler) {
		this(new ArgumentParser(), achievementHandler);
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
		Gdx.gl.glClearColor(Constants.COLOR_YELLOW.r, Constants.COLOR_YELLOW.g, Constants.COLOR_YELLOW.b,
				Constants.COLOR_YELLOW.a);
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
