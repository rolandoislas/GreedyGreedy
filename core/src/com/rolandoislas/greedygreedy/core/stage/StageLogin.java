package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;
import com.rolandoislas.greedygreedy.core.ui.CallbackDialog;
import com.rolandoislas.greedygreedy.core.ui.skin.DialogSkin;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

public class StageLogin extends Stage implements DialogCallbackHandler {
    private final Stage successStage;
    private final Stage failStage;
    private final CallbackDialog waitingMessage;
    private String authCode;
    private boolean skipPrompt;
    private DialogResult dialogResult;

    public StageLogin(final Stage _successStage, Stage failStage, String authCode) {
        this.successStage = _successStage;
        this.failStage = failStage;
        this.authCode = authCode;
        // Check for refresh token or access token
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        skipPrompt = preferences.contains(Constants.PREF_ACCESS_TOKEN) ||
                preferences.contains(Constants.PREF_REFRESH_TOKEN) ||
                Gdx.app.getType().equals(Application.ApplicationType.Android);
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Login prompt
        CallbackDialog loginPrompt = new CallbackDialog("Login/Signup", new DialogSkin());
        loginPrompt.getTitleTable().row();
        loginPrompt.getTitleTable().add("An external browser will be launched.");
        loginPrompt.button("Ok", true);
        loginPrompt.button("Cancel", false);
        if (authCode.isEmpty() && !skipPrompt) {
            dialogResult = DialogResult.LOGIN;
            loginPrompt.show(this);
        }
        // Waiting message
        waitingMessage = new CallbackDialog("Waiting for Login", new DialogSkin());
        waitingMessage.button("Cancel");
    }

    public StageLogin(Stage successStage, Stage failStage) {
        this(successStage, failStage, "");
    }

    @Override
    public void act() {
        super.act();
        if (!authCode.isEmpty() || skipPrompt) {
            dialogResult = DialogResult.WAITING;
            waitingMessage.show(this);
            GreedyClient.authenticationHandler.login(new Runnable() {
                @Override
                public void run() {
                    GreedyClient.setStage(successStage);
                }
            }, authCode);
            skipPrompt = false;
            authCode = "";
        }
    }

    @Override
    public void onBackButtonPressed() {
        GreedyClient.authenticationHandler.cancel();
        if (failStage != null)
            GreedyClient.setStage(failStage);
        else
            GreedyClient.setStage(new StageMenu());
    }

    @Override
    public void dialogResult(Object object) {
        switch (dialogResult) {
            case LOGIN:
                boolean userInitiatedLogin = Boolean.parseBoolean(String.valueOf(object));
                if (userInitiatedLogin) {
                    dialogResult = DialogResult.WAITING;
                    waitingMessage.show(this);
                    GreedyClient.authenticationHandler.login(new Runnable() {
                        @Override
                        public void run() {
                            GreedyClient.setStage(successStage);
                        }
                    });
                }
                else
                    onBackButtonPressed();
                break;
            case WAITING:
                onBackButtonPressed();
                break;
        }

    }

    private enum DialogResult {
        WAITING, LOGIN
    }

    @Override
    public Color getBackgroundColor() {
        return Constants.COLOR_YELLOW;
    }
}
