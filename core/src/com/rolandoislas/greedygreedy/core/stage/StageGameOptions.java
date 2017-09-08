package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;
import com.rolandoislas.greedygreedy.core.ui.CallbackDialog;
import com.rolandoislas.greedygreedy.core.ui.skin.DialogSkin;
import com.rolandoislas.greedygreedy.core.util.*;

import java.util.ArrayList;

public class StageGameOptions extends Stage implements DialogCallbackHandler {
    private final Label playersNum;
    private final Label message;
    private final Label botsVal;
    private final Label privateGameVal;
    private final Label gameTypeVal;
    private final boolean singlePlayer;
    private final PlayerInfoCard playerInfoCard;
    private final DialogSkin dialogSkin;
    private DialogResult dialogResult;
    private CallbackDialog dialog;

    public StageGameOptions(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Icon
        playerInfoCard = new PlayerInfoCard();
        playerInfoCard.setName(PreferencesUtil.getPlayerName());
        playerInfoCard.setSize(getWidth() / 2 - getWidth() / 2 * .06f, getWidth() / 6);
        playerInfoCard.setPosition(getWidth() / 2 - playerInfoCard.getWidth() / 2,
                title.getY() - playerInfoCard.getHeight() * 1.5f);
        playerInfoCard.setIcon(PreferencesUtil.getIcon());
        playerInfoCard.showGearIcon(true);
        playerInfoCard.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageStore());
            }
        });
        addActor(playerInfoCard);
        // Async Icon update
        Thread iconUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Icon icon = null;
                try {
                    icon = GreedyApi.getIcon();
                } catch (GreedyException e) {
                    Logger.exception(e);
                }
                if (icon == null)
                    return;
                final Icon finalIcon = icon;
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        playerInfoCard.setIcon(finalIcon);
                    }
                });
                PreferencesUtil.setIcon(icon);
            }
        });
        iconUpdateThread.setDaemon(true);
        iconUpdateThread.setName("Icon Update");
        iconUpdateThread.start();
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
        playersNum.setSize(getWidth() * .25f, playersNum.getHeight());
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
        botsVal.setSize(getWidth() * .25f, botsVal.getHeight());
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
        privateGameVal.setSize(getWidth() * .25f, privateGameVal.getHeight());
        privateGameVal.setColor(Color.LIGHT_GRAY);
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
        // Dialog skin
        dialogSkin = new DialogSkin();
    }

    @Override
    public void dispose() {
        super.dispose();
        playerInfoCard.dispose();
        dialogSkin.dispose();
    }

    private void showGameTypeDialog() {
        dialog = new CallbackDialog("Game Type", dialogSkin);
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
        GameOptionsUtil.PointValue points = GameOptionsUtil.parseOptions(players, enableBots, privateGame, gameType);
        // Points message
        if (players == 1 && !enableBots)
            message.setText("Cannot start a 1 person game with no bots.");
        else if (points.equals(GameOptionsUtil.PointValue.NOT_ENOUGH_PLAYERS))
            message.setText("No points will be awarded for this match: Not enough players.");
        else if (points.equals(GameOptionsUtil.PointValue.PRIVATE_GAME))
            message.setText("No points will be awarded for this match: Private game.");
        else if (!points.equals(GameOptionsUtil.PointValue.FULL_POINTS))
            message.setText("No points will be awarded for this match.");
        else
            message.setText("");
        // Start
        if (doStart && (players > 1 || (players == 1 && enableBots)))
            GreedyClient.setStage(new StageGame(players, privateGame, enableBots, gameType, singlePlayer));
    }

    private void showPlayersDialog() {
        dialog = new CallbackDialog("Players", dialogSkin);
        for (int playerNum = 0; playerNum < Constants.MAX_PLAYERS; playerNum++) {
            dialog.getButtonTable().row();
            dialog.button("Players: " + String.valueOf(playerNum + 1), playerNum + 1);
        }
        dialogResult = DialogResult.PLAYERS;
        dialog.show(this);
    }

    @Override
    public void onBackButtonPressed() {
        if (dialog != null) {
            dialog.hide(null);
            dialog = null;
        }
        else
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
        dialog = null;
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
