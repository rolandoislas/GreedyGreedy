package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

/**
 * Created by rolando on 7/16/17.
 */
public class StageMenu extends Stage {

    public StageMenu() {
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Singleplayer button
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(1);
        TextButton buttonSinglePlayer = new TextButton("Singleplayer", tbs);
        float buttonOffset = getHeight() * .3f;
        buttonSinglePlayer.setPosition(getWidth() / 2 - buttonSinglePlayer.getWidth() / 2,
                title.getY() - buttonSinglePlayer.getHeight() - buttonOffset);
        buttonSinglePlayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageGame(1, true));
            }
        });
        addActor(buttonSinglePlayer);
        // Multiplayer
        TextButton buttonMultiplayer = new TextButton("Multiplayer", tbs);
        buttonMultiplayer.setPosition(getWidth() / 2 - buttonMultiplayer.getWidth() / 2 ,
                buttonSinglePlayer.getY() - buttonMultiplayer.getHeight());
        buttonMultiplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO make this move to the multiplayer search/host stage
            }
        });
        addActor(buttonMultiplayer);
        // Info
        TextButton buttonInfo = new TextButton("Info", tbs);
        buttonInfo.setPosition(getWidth() / 2 - buttonInfo.getWidth() / 2,
                buttonMultiplayer.getY() - buttonInfo.getHeight());
        buttonInfo.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO info screen
            }
        });
        addActor(buttonInfo);
    }

    @Override
    public void onBackButtonPressed() {
        if (!Gdx.app.getType().equals(Application.ApplicationType.Desktop))
            Gdx.app.exit();
    }
}
