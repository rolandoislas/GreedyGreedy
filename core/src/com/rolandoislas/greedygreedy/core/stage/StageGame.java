package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.DieActor;
import com.rolandoislas.greedygreedy.core.actor.HidableLog;
import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;
import com.rolandoislas.greedygreedy.core.net.WebClient;
import com.rolandoislas.greedygreedy.core.util.*;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by rolando on 7/16/17.
 */
public class StageGame extends Stage implements ControlEventListener {
    private final GameController gameController;
    private final boolean singlePlayer;
    private ArrayList<DieActor> dice;
    private ArrayList<PlayerInfoCard> playerInfoCards;
    private Label title;
    private int whoami = -1;
    private ArrayList<Player> players;
    private Label message;
    private MessageClearThread messageClearThread;
    private HidableLog log;
    private ImageButton buttonLog;
    private boolean gameOver;

    public StageGame(int numberOfPlayers, boolean privateGame, boolean enableBots, GameController.GameType gameType,
                     boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        // Init
        createDice();
        createGui();
        createPlayers();
        createLog();
        // Start game controller
        if (singlePlayer) {
            gameController = new AiController(numberOfPlayers, gameType, enableBots, singlePlayer);
            Preferences save = PreferencesUtil.get(Constants.PREF_CATEGORY_SAVE);
            if (save.contains(Constants.PREF_GAMESTATE_SINGLE_PLAYER) &&
                    !save.getString(Constants.PREF_GAMESTATE_SINGLE_PLAYER).isEmpty()) {
                gameController.loadState(save.getString(Constants.PREF_GAMESTATE_SINGLE_PLAYER));
                save.remove(Constants.PREF_GAMESTATE_SINGLE_PLAYER);
                save.flush();
            }
        }
        else
            gameController = new WebClient(numberOfPlayers, privateGame);
        gameController.addListener(this);
        gameController.start();
        // Click listener
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stageClicked(event, x, y);
            }
        });
    }

    private void stageClicked(InputEvent event, float x, float y) {
        if (!log.isHidden())
            log.setHidden(true);
        else if (log.isHidden() && x >= buttonLog.getX() && x <= buttonLog.getX() + getWidth() && y >= buttonLog.getY() &&
                y <= buttonLog.getY() + buttonLog.getHeight())
            log.setHidden(false);
    }

    private void createLog() {
        // Log button
        Skin skin = new Skin();
        skin.add("button", new Texture("image/log_button.png"));
        ImageButton.ImageButtonStyle ibs = new ImageButton.ImageButtonStyle();
        ibs.imageUp = skin.getDrawable("button");
        buttonLog = new ImageButton(ibs);
        buttonLog.setBounds(0, 0, getWidth() * .1f, getWidth() * .1f);
        addActor(buttonLog);
        // Log
        skin.add("background", new Texture("image/log_background.png"));
        skin.add("selection", new Texture("image/transparent.png"));
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = TextUtil.generateScaledFont(0.25f);
        listStyle.background = skin.getDrawable("background");
        listStyle.selection = skin.getDrawable("selection");
        log = new HidableLog(listStyle);
        log.setBounds(-getWidth(), 0, getWidth(), getHeight());
        log.setHidden(true);
        addActor(log);
    }

    private void createPlayers() {
        playerInfoCards = new ArrayList<PlayerInfoCard>();
        for (int playerNum = 0; playerNum < Constants.MAX_PLAYERS; playerNum++) {
            PlayerInfoCard player = new PlayerInfoCard();
            float width = getWidth() / 2 - getWidth() / 2 * .06f;
            float height = dice.get(0).getHeight();
            float marginLeft = getWidth() / 2 * .05f;
            float marginCenter = getHeight() / 2 * .02f;
            float marginTop = getHeight() * .06f;
            if (playerNum == 0) {
                player.setBounds(marginLeft, title.getY() - height - marginTop, width, height);
            }
            else if (playerNum == 3) {
                player.setBounds(marginLeft + width + marginCenter, playerInfoCards.get(0).getY(), width, height);
            }
            else {
                PlayerInfoCard previousPlayer = playerInfoCards.get(playerNum - 1);
                player.setBounds(previousPlayer.getX(),
                        previousPlayer.getY() - height - marginCenter, width, height);
            }
            playerInfoCards.add(player);
            addActor(playerInfoCards.get(playerNum));
        }
    }

    private void createGui() {
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
        // Roll button
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(1);
        TextButton buttonRoll = new TextButton("Roll", tbs);
        DieActor die = dice.get(1);
        buttonRoll.setPosition(die.getX() + die.getWidth() / 2 - buttonRoll.getWidth() / 2,
                die.getY() - buttonRoll.getHeight());
        buttonRoll.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                rollClicked(event, x, y);
            }
        });
        addActor(buttonRoll);
        // Stop button
        TextButton buttonStop = new TextButton("Stop", tbs);
        die = dice.get(3);
        buttonStop.setPosition(die.getX() + die.getWidth() / 2 - buttonStop.getWidth() / 2,
                die.getY() - buttonStop.getHeight());
        buttonStop.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stopClicked(event, x, y);
            }
        });
        addActor(buttonStop);
        // Message
        Label.LabelStyle lsm = new Label.LabelStyle();
        lsm.font = TextUtil.generateScaledFont(0.5f);
        message = new Label("", lsm);
        message.setBounds(0, buttonRoll.getY() - lsm.font.getLineHeight() * 2 - getHeight() * .02f,
                getWidth(), lsm.font.getLineHeight() * 2);
        message.setAlignment(Align.center, Align.top);
        message.setWrap(true);
        addActor(message);
    }

    private void createDice() {
        this.dice = new ArrayList<DieActor>();
        int numberOfDice = 5;
        int dieSize = (int) (getWidth() / (numberOfDice + 1));
        int diePadding = (int) (dieSize * .1f);
        for (int dieNum = 0; dieNum < numberOfDice; dieNum++) {
            final DieActor die = new DieActor();
            if (dieNum > 0) {
                DieActor previousDie = dice.get(dieNum - 1);
                die.setBounds(previousDie.getX() + dieSize + diePadding, previousDie.getY(), dieSize, dieSize);
            }
            else {
                float x = (getWidth() - (numberOfDice * dieSize + diePadding * (numberOfDice - 1))) / 2;
                float y = getHeight() / 2 - dieSize - getHeight() * .07f;
                die.setBounds(x, y, dieSize, dieSize);
            }
            die.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dieClicked(event, x, y, die);
                }
            });
            this.dice.add(die);
            this.addActor(this.dice.get(dieNum));
        }
    }

    private void stopClicked(InputEvent event, float x, float y) {
        if (isPlayersTurn())
            gameController.stopClicked();
        else
            setMessage(true, false, "It's not your turn.");
    }

    private void rollClicked(InputEvent event, float x, float y) {
        if (isPlayersTurn())
            gameController.rollClicked();
        else
            setMessage(true, false, "It's not your turn.");
    }

    private void dieClicked(InputEvent event, float x, float y, DieActor die) {
        if (isPlayersTurn())
            gameController.clickDie(dice.indexOf(die));
        else
            setMessage(true, false, "It's not your turn.");
    }

    private boolean isPlayersTurn() {
        return players.get(whoami).isActive();
    }

    @Override
    public void resize(int width, int height) {
        this.getViewport().update(width, height);
    }

    @Override
    public void onBackButtonPressed() {
        if (singlePlayer)
            GreedyClient.setStage(new StageMenu());
        // Ignore back button if the game is multiplayer
        else
            setMessage(true, false, "Cannot exit multiplayer game.");
    }

    @Override
    public void connected() {

    }

    @Override
    public void disConnected(String reason) {
        Logger.debug(reason);
    }

    @Override
    public void dieUpdate(ArrayList<IDie> dice) {
        for (IDie die : dice)
            this.dice.get(dice.indexOf(die)).setStats(die);
    }

    @Override
    public void playerUpdate(ArrayList<Player> players) {
        this.players = players;
        for (Player player : this.players)
            playerInfoCards.get(this.players.indexOf(player)).setStats(player);
    }

    @Override
    public void whoami(int playerNum) {
        this.whoami = playerNum;
    }

    @Override
    public void actionFailed(Action action, FailReason failReason) {
        switch (action) {
            case DIE:
                switch (failReason) {
                    case TURN_NOT_STARTED:
                        setMessage(false, false, "Roll first.");
                        break;
                    default:
                        Logger.debug("Unhandled failed reason: %s for action %s", failReason.toString(),
                                action.toString());
                }
                break;
            case ROLL:
                switch (failReason) {
                    case NO_PLAYABLE_VALUES:
                        setMessage(false, false, "Ziltch!"); // TODO log it and get current players name
                        break;
                    case NO_SELECTION:
                        setMessage(false, false, "No dice selected!");
                        break;
                    case NO_SCORE:
                        setMessage(false, false, "Selection is invalid.");
                        break;
                    default:
                        Logger.debug("Unhandled failed reason: %s for action %s", failReason.toString(),
                                action.toString());
                }
                break;
            case STOP:
                switch (failReason) {
                    case TURN_NOT_STARTED:
                        setMessage(false, false, "Roll first.");
                        break;
                    case NOT_ENOUGH_POINTS:
                        setMessage(false, false, "Not enough points to stop.");
                        break;
                    case NO_SELECTION:
                        setMessage(false, false, "Selection is invalid.");
                        break;
                    default:
                        Logger.debug("Unhandled failed reason: %s for action %s", failReason.toString(),
                                action.toString());
                }
                break;
            default:
                Logger.debug("Unhandled failed action: %s", action.toString());
        }
    }

    /**
     * Sets a message to the message label and adds it to the log.
     * @param force Should the message be handled even if the user is not active (not their turn)
     * @param log Should the message be added to the log
     * @param message message to display and log
     */
    private void setMessage(boolean force, boolean log, String message) {
        if (!force && getActivePlayer() != whoami)
            return;
        if (messageClearThread != null && messageClearThread.isAlive())
            messageClearThread.interrupt();
        messageClearThread = new MessageClearThread();
        messageClearThread.start();
        this.message.setText(message);
        if (log) {
            this.log.add(message);
        }
    }

    @Override
    public void activePoints(int activePoints) {
        playerInfoCards.get(getActivePlayer()).setTempScore(activePoints);
    }

    @Override
    public void turnEnd(int player, int points) {
        String name = players.get(player).getName();
        if (points > 0)
            setMessage(true, true, String.format(Locale.US,"%s ended their turn for %d points.",
                    name, points));
        else
            setMessage(true, true, String.format(Locale.US, "%s ziltched!", name));
    }

    @Override
    public void gameEnd(ArrayList<Player> players) {
        gameOver = true;
        GreedyClient.setStage(new StagePostGame(players));
    }

    @Override
    public void lastRound(int lastRoundStarter) {
        setMessage(true, true, String.format(Locale.US, "%s is about to win. Last round started!",
                players.get(lastRoundStarter).getName()));
    }

    @Override
    public void zilchWarning(int player) {
        setMessage(true, true, String.format(Locale.US,
                "%s will have their points set to 0 if they zilch again.", players.get(player).getName()));
        // TODO zilch sound
    }

    @Override
    public void achievement(AchievementHandler.Achievement achievement, int player) {
        if (player == whoami)
            GreedyClient.achievementHandler.give(achievement);
    }

    @Override
    public void dispose() {
        super.dispose();
        gameController.stop();
        // Save state
        if (singlePlayer && !gameOver) {
            String gameState = gameController.saveState();
            Preferences save = PreferencesUtil.get(Constants.PREF_CATEGORY_SAVE);
            save.putString(Constants.PREF_GAMESTATE_SINGLE_PLAYER, gameState);
            save.flush();
        }
    }

    private int getActivePlayer() {
        for (Player player : players)
            if (player.isActive())
                return players.indexOf(player);
        return -1;
    }

    private class MessageClearThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                message.setText("");
            } catch (InterruptedException ignore) {}
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        gameController.act(delta);
    }

    @Override
    public Color getBackgroundColor() {
        return Constants.COLOR_YELLOW;
    }
}
