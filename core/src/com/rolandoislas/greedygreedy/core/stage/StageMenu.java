package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.ui.CallbackDialog;
import com.rolandoislas.greedygreedy.core.ui.skin.DialogSkin;
import com.rolandoislas.greedygreedy.core.util.GameController;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

/**
 * Created by rolando on 7/16/17.
 */
public class StageMenu extends Stage {

    private final TextButton buttonAuth;
    private boolean hasActed;

    public StageMenu(String message) {
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
        float buttonOffset = getHeight() * .2f;
        buttonSinglePlayer.setPosition(getWidth() / 2 - buttonSinglePlayer.getWidth() / 2,
                title.getY() - buttonSinglePlayer.getHeight() - buttonOffset);
        buttonSinglePlayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (PreferencesUtil.get(Constants.PREF_CATEGORY_SAVE).contains(Constants.PREF_GAMESTATE_SINGLE_PLAYER))
                    GreedyClient.setStage(new StageGame(1, true, true,
                            GameController.GameType.ANY, true));
                else
                    GreedyClient.setStage(new StageGameOptions(true));
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
                    GreedyClient.setStage(new StageLogin(new StageGameOptions(false), null));
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
        // Auth
        buttonAuth = new TextButton("", tbs);
        buttonAuth.setPosition(getWidth() / 2 - buttonAuth.getWidth() / 2,
                buttonInfo.getY() - buttonAuth.getHeight());
        buttonAuth.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Logout
                if (PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL).contains(Constants.PREF_ACCESS_TOKEN)) {
                    GreedyClient.authenticationHandler.logout();
                    GreedyClient.setStage(new StageMenu());
                }
                // Login
                else
                    GreedyClient.setStage(new StageLogin(new StageMenu(), null));
            }
        });
        addActor(buttonAuth);
        // Message
        if (message != null && !message.isEmpty()) {
            CallbackDialog messageDialog = new CallbackDialog(message, new DialogSkin());
            messageDialog.button("Ok");
            messageDialog.show(this);
        }
    }

    public StageMenu() {
        this("");
    }

    @Override
    public void act() {
        super.act();
        if (!hasActed) {
            hasActed = true;
            buttonAuth.setText(PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL)
                    .contains(Constants.PREF_ACCESS_TOKEN) ? "Sign Out" : "Sign In");
        }
    }

    @Override
    public void onBackButtonPressed() {
        if (!Gdx.app.getType().equals(Application.ApplicationType.Desktop))
            Gdx.app.exit();
    }

    @Override
    public Color getBackgroundColor() {
        return Color.BLACK;
    }
}
