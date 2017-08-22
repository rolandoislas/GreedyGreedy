package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Rolando on 2/14/2017.
 */
public class StageLoad extends Stage {
	private final Image icon;
	private final Label loading;
	private final boolean doLoad;
	private int skipTicks = 1;
	private ArrayList<Float> fontSizes = new ArrayList<Float>(Arrays.asList(1f, 1.25f, 0.25f, 0.5f));
	private int percent = 0;
	private boolean doGenerate;

	/**
	 * Loading stage
	 * @param doLoad should resources be loaded? It acts as a splash screen otherwise.
	 */
	public StageLoad(boolean doLoad) {
		setBackgroundColor(Color.BLACK);
		this.doLoad = doLoad;
		// Create icon
		icon = new Image(new Texture("image/icon_512.png")); // TODO set icon
		float size = Gdx.graphics.getHeight();
		if (Gdx.graphics.getHeight() > Gdx.graphics.getWidth())
			size = Gdx.graphics.getWidth();
		size *= .8f;
		icon.setSize(size, size);
		icon.setPosition(Gdx.graphics.getWidth() / 2f - size / 2f,
				Gdx.graphics.getHeight() / 2f - icon.getHeight() / 2f);
		addActor(icon);
		// Loading Text
		Label.LabelStyle loadingStyle = new Label.LabelStyle();
		loadingStyle.font = new BitmapFont(Gdx.files.internal("font/collvetica.fnt"));
		loadingStyle.font.getData().setScale((Gdx.graphics.getHeight() * .05f) / loadingStyle.font.getLineHeight());
		loadingStyle.fontColor = Color.WHITE;
		loading = new Label(Constants.NAME, loadingStyle);
		loading.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * .2f);
		loading.setAlignment(Align.center);
		addActor(loading);
	}

	@SuppressWarnings("unused")
	public StageLoad() {
		this(true);
	}

	@Override
	public void act(float delta) {
		if (!doLoad)
			return;
		if (this.skipTicks > 0) {
			this.skipTicks--;
			return;
		}
		if (doGenerate) {
			TextUtil.generateScaledFont(fontSizes.get(0));
			fontSizes.remove(0);
			doGenerate = false;
			skipTicks++;
			percent += 25;
			return;
		}
		else if (fontSizes.size() > 0) {
			loading.setText(String.format(Locale.US,"Generating Fonts %d%%", percent));
			doGenerate = true;
			skipTicks++;
			return;
		}
		//loading.setText(String.format(Locale.US,"Launching %d%%", 100));

		GreedyClient.setStage(new StageMenu());
	}

	@Override
	public void onBackButtonPressed() {
		Gdx.app.exit();
	}
}
