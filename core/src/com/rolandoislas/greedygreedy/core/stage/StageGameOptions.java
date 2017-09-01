package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;
import com.rolandoislas.greedygreedy.core.ui.CallbackDialog;
import com.rolandoislas.greedygreedy.core.ui.skin.DialogSkin;
import com.rolandoislas.greedygreedy.core.util.GameController;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

import java.util.ArrayList;

public class StageGameOptions extends Stage implements DialogCallbackHandler {
    private final Label playersNum;
    private final Label message;
    private final Label botsVal;
    private final Label privateGameVal;
    private final Label gameTypeVal;
    private final boolean singlePlayer;
    private DialogResult dialogResult;

    public StageGameOptions(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Icon
        PlayerInfoCard playerInfoCard = new PlayerInfoCard();
        playerInfoCard.setName(PreferencesUtil.getPlayerName());
        playerInfoCard.setSize(getWidth() / 2 - getWidth() / 2 * .06f, getWidth() / 6);
        playerInfoCard.setPosition(getWidth() / 2 - playerInfoCard.getWidth() / 2,
                title.getY() - playerInfoCard.getHeight() * 1.5f);
        addActor(playerInfoCard);
        // Players
        Label.LabelStyle optionsStyle = new Label.LabelStyle();
        optionsStyle.font = TextUtil.generateScaledFont(0.5f);
        Label players = new Label("Players", optionsStyle);
        players.setPosition(title.getX(), playerInfoCard.getY() - players.getHeight() -
                playerInfoCard.getHeight() / 2);
        if (!singlePlayer)
            players.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPlayersDialog();
                }
            });
        addActor(players);
        // Players num
        playersNum = new Label(singlePlayer ? "1" : String.valueOf(Constants.MAX_PLAYERS), optionsStyle);
        float optionsValueX = getWidth() / 2;
        playersNum.setPosition(optionsValueX, players.getY());
        if (!singlePlayer)
            playersNum.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPlayersDialog();
                }
            });
        addActor(playersNum);
        // Bots
        Label bots = new Label("Bots", optionsStyle);
        bots.setPosition(players.getX(), players.getY() - bots.getHeight() -
                players.getHeight() / 2);
        if (!singlePlayer)
            bots.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    toggleBots();
                }
            });
        addActor(bots);
        // Bots val
        botsVal = new Label(singlePlayer ? "on" : "off", optionsStyle);
        botsVal.setPosition(optionsValueX, bots.getY());
        if (!singlePlayer)
            botsVal.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    toggleBots();
                }
            });
        addActor(botsVal);
        // Private Game
        Label privateGame = new Label("Private Game", optionsStyle);
        privateGame.setPosition(bots.getX(), bots.getY() - privateGame.getHeight() -
                bots.getHeight() / 2);
        if (!singlePlayer)
            privateGame.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    togglePrivate();
                }
            });
        addActor(privateGame);
        // Private game val
        privateGameVal = new Label(singlePlayer ? "on" : "off", optionsStyle);
        privateGameVal.setPosition(optionsValueX, privateGame.getY());
        if (!singlePlayer)
            privateGameVal.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    //togglePrivate(); // TODO handle private games
                }
            });
        addActor(privateGameVal);
        // Game type
        Label gameType = new Label("Game Type", optionsStyle);
        gameType.setPosition(privateGame.getX(), privateGame.getY() - gameType.getHeight() -
                privateGame.getHeight() / 2);
        gameType.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showGameTypeDialog();
            }
        });
        addActor(gameType);
        // Game type val
        gameTypeVal = new Label("Normal", optionsStyle);
        gameTypeVal.setPosition(optionsValueX, gameType.getY());
        gameTypeVal.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showGameTypeDialog();
            }
        });
        addActor(gameTypeVal);
        // Start game
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(1f);
        final TextButton startGame = new TextButton("Start Game", tbs);
        startGame.setPosition(getWidth() / 2 - startGame.getWidth() / 2, 0);
        startGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGame(true);
            }
        });
        addActor(startGame);
        // Message
        Label.LabelStyle messageStyle = new Label.LabelStyle();
        messageStyle.font = TextUtil.generateScaledFont(0.25f);
        message = new Label("", messageStyle);
        message.setBounds(0, startGame.getHeight(), getWidth(), message.getHeight());
        message.setAlignment(Align.center);
        addActor(message);
        startGame(false);
    }

    private void showGameTypeDialog() {
        CallbackDialog dialog = new CallbackDialog("Game Type", new DialogSkin());
        ArrayList<String> types = new ArrayList<String>();
        types.add("Normal");
        types.add("Zilch");
        if (!singlePlayer)
            types.add("Any");
        for (String type : types) {
            dialog.getButtonTable().row();
            dialog.button(type, type);
        }
        dialogResult = DialogResult.GAME_TYPE;
        dialog.show(this);
    }

    private void togglePrivate() {
        privateGameVal.setText(privateGameVal.getText().toString().equalsIgnoreCase("on") ? "off" : "on");
        startGame(false);
    }

    private void toggleBots() {
        botsVal.setText(botsVal.getText().toString().equalsIgnoreCase("on") ? "off" : "on");
        startGame(false);
    }

    private void startGame(boolean doStart) {
        // Parse the values and warn if points will not be awarded
        int players = Integer.parseInt(String.valueOf(playersNum.getText()));
        boolean enableBots = botsVal.getText().toString().equalsIgnoreCase("on");
        boolean privateGame = privateGameVal.getText().toString().equalsIgnoreCase("on");
        // Game type
        GameController.GameType gameType;
        String gameTypeString = gameTypeVal.getText().toString();
        if (gameTypeString.equalsIgnoreCase("normal"))
            gameType = GameController.GameType.NORMAL;
        else if (gameTypeString.equalsIgnoreCase("zilch"))
            gameType = GameController.GameType.ZILCH;
        else
            gameType = GameController.GameType.ANY;
        // Points message
        if (players == 1 && !enableBots)
            message.setText("No points will be awarded for this match: Not enough players.");
        if (privateGame)
            message.setText("No points will be awarded for this match: Private game.");
        // Start
        if (doStart)
            GreedyClient.setStage(new StageGame(players, privateGame, enableBots, gameType, singlePlayer));
    }

    private void showPlayersDialog() {
        CallbackDialog dialog = new CallbackDialog("Players", new DialogSkin());
        for (int playerNum = 0; playerNum < Constants.MAX_PLAYERS; playerNum++) {
            dialog.getButtonTable().row();
            dialog.button(String.valueOf(playerNum + 1), playerNum + 1);
        }
        dialogResult = DialogResult.PLAYERS;
        dialog.show(this);
    }

    @Override
    public void onBackButtonPressed() {
        GreedyClient.setStage(new StageMenu());
    }

    @Override
    public void dialogResult(Object object) {
        switch (dialogResult) {
            case PLAYERS:
                playersNum.setText(String.valueOf(object));
                break;
            case GAME_TYPE:
                gameTypeVal.setText(String.valueOf(object));
                break;
        }
        startGame(false);
    }

    private enum DialogResult {
        GAME_TYPE, PLAYERS
    }

    @Override
    public Color getBackgroundColor() {
        return Constants.COLOR_YELLOW;
    }
}
