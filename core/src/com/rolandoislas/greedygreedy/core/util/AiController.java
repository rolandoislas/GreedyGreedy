package com.rolandoislas.greedygreedy.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Die;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class AiController extends GameControllerBase implements GameController {
    private static final int[] SCORE_THREE_OR_MORE = new int[] {1000, 200, 300, 400, 500, 600};
    private static final int SCORE_ONE = 100;
    private static final int SCORE_FIVE = 50;
    private static final int[] SCORE_PAIR = new int[] {200, 50, 50, 50, 100, 50};
    private static final int SCORE_START_THRESHOLD = 1000;
    public static final int SCORE_WIN = 10000;
    private static final int ZILCH_MAX = 2;
    private boolean singlePlayer;
    private int numberOfPlayers;
    private GameType gameType;
    private boolean enableBots;
    private ArrayList<Player> players = new ArrayList<Player>();
    private Random random = new Random();
    private ArrayList<Die> dice = new ArrayList<Die>();
    private boolean turnStarted;
    private int activePoints;
    private boolean lastRound;
    private int lastRoundStarter;
    private boolean handleActions;
    private ReentrantLock mutex = new ReentrantLock();
    private ArrayList<String> names;
    private boolean gameOver;
    private ArrayList<Icon> icons;

    public AiController(int numberOfPlayers, GameType gameType, boolean enableBots, boolean singlePlayer) {
        this.numberOfPlayers = numberOfPlayers;
        this.gameType = gameType;
        this.enableBots = enableBots;
        this.singlePlayer = singlePlayer;
        names = new ArrayList<String>();
        icons = new ArrayList<Icon>();
    }

    @Override
    public void start() {
        if (singlePlayer) {
            sendConnected();
            sendWhoami(0);
        }
        if (players.size() == 0)
            populatePlayers();
        if (dice.size() == 0)
            resetDice();
        handleActions = true;
        // Send the state
        if (singlePlayer) {
            sendPlayerUpdate(players);
            sendDieUpdate(dice);
            sendActivePoints(activePoints);
        }
    }

    private void resetDice() {
        dice.clear();
        for (int dieNum = 0; dieNum < 5; dieNum++)
            dice.add(new Die());
    }

    @Override
    public void clickDie(int dieNum) {
        if (!handleActions)
            return;
        mutex.lock();
        if (!turnStarted) {
            sendFailUpdate(ControlEventListener.Action.DIE, ControlEventListener.FailReason.TURN_NOT_STARTED,
                    getActivePlayer());
            mutex.unlock();
            return;
        }
        if (dieNum >= dice.size() || dieNum < 0)
            return;
        Die die = dice.get(dieNum);
        if (!die.isLocked()) {
            if (die.isSelected()) {
                die.setSelected(false);
            }
            else {
                die.setSelected(true);
            }
        }
        sendDieUpdate(dice);
        // Send the current active points
        ArrayList<Integer> selected = new ArrayList<Integer>();
        for (Die sdie : dice)
            if (sdie.isSelected())
                selected.add(sdie.getFace());
        int rollScore = activePoints;
        if (selected.size() > 0)
            rollScore += scoreSelected(selected);
        sendActivePoints(rollScore);
        mutex.unlock();
    }

    @Override
    public void rollClicked() {
        if (!handleActions)
            return;
        mutex.lock();
        // Start new turn
        if (!turnStarted) {
            activePoints = 0;
            // Set the dice faces
            resetDice();
            for (Die die : dice)
                die.setFace(random.nextInt(6) + 1);
            turnStarted = true;
            if (!diceHavePlayableValue()) {
                activePoints = 0;
                sendAchievement(AchievementHandler.Achievement.INSTANT_ZILCH, getActivePlayer());
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_PLAYABLE_VALUES,
                        getActivePlayer());
                handleZilch(true);
                setNextPlayerActive();
                sendPlayerUpdate(players);
            }
            else
                sendRollSuccess(getActivePlayer());
        }
        // Start a roll
        else {
            // Get selected
            ArrayList<Integer> selected = new ArrayList<Integer>();
            for (Die die : dice)
                if (die.isSelected())
                    selected.add(die.getFace());
            // Do nothing if there was no selection
            if (selected.size() == 0) {
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_SELECTION,
                        getActivePlayer());
                mutex.unlock();
                return;
            }
            // Calculate roll score
            int rollScore = scoreSelected(selected);
            if (rollScore > 0) {
                activePoints += rollScore;
            }
            else {
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_SCORE,
                        getActivePlayer());
                mutex.unlock();
                return;
            }
            // Lock and roll dice
            boolean rolledDie = false;
            for (Die die : dice) {
                if (die.isSelected()) {
                    die.setLocked(true);
                    die.setSelected(false);
                }
                if (!die.isLocked()) {
                    die.setFace(random.nextInt(6) + 1);
                    rolledDie = true;
                }
            }
            // All dice have been selected or locked. Roll them all.
            if (!rolledDie) {
                sendAchievement(AchievementHandler.Achievement.ANOTHER_ROLL, getActivePlayer());
                resetDice();
                for (Die die : dice)
                    die.setFace(random.nextInt(6) + 1);
            }
            // Check the post roll for playable values
            if (!diceHavePlayableValue()) {
                sendAchievement(AchievementHandler.Achievement.ZILCH, getActivePlayer());
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_PLAYABLE_VALUES,
                        getActivePlayer());
                handleZilch(true);
                activePoints = 0;
                setNextPlayerActive();
            }
            else
                sendRollSuccess(getActivePlayer());
            sendActivePoints(activePoints);
            sendPlayerUpdate(players);
        }
        sendDieUpdate(dice);
        mutex.unlock();
    }

    private void handleZilch(boolean didZilch) {
        if (!gameType.equals(GameType.ZILCH))
            return;
        Player player = players.get(getActivePlayer());
        if (player.getScore() == 0)
            return;
        if (didZilch)
            player.setZilchAmount(player.getZilchAmount() + 1);
        else
            player.setZilchAmount(0);
        if (player.getZilchAmount() > ZILCH_MAX) {
            player.setScore(0);
            player.setZilchAmount(0);
            sendAchievement(AchievementHandler.Achievement.ZILCH_RESET, getActivePlayer());
        }
        else if (player.getZilchAmount() == ZILCH_MAX)
            sendZilchWarning(getActivePlayer());
    }

    private int getActivePlayer() {
        for (Player player : players)
            if (player.isActive())
                return players.indexOf(player);
        return -1;
    }

    @Override
    public void stopClicked() {
        if (!handleActions)
            return;
        mutex.lock();
        if (!turnStarted) {
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.TURN_NOT_STARTED,
                    getActivePlayer());
            mutex.unlock();
            return;
        }

        // Get selected
        ArrayList<Integer> selected = new ArrayList<Integer>();
        for (Die die : dice)
            if (die.isSelected())
                selected.add(die.getFace());
        int rollPoints = scoreSelected(selected);
        if (rollPoints == 0) {
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.NO_SELECTION,
                    getActivePlayer());
            mutex.unlock();
            return;
        }
        // Check player is above point threshold
        Player player = players.get(getActivePlayer());
        boolean hasMaxScore = true;
        for (Player p : players)
            if (p != player && p.getScore() >= player.getScore() + activePoints + rollPoints)
                hasMaxScore = false;
        if ((player.getScore() >= SCORE_START_THRESHOLD || activePoints + rollPoints >= SCORE_START_THRESHOLD) &&
                (!lastRound || hasMaxScore)) {
            activePoints += rollPoints;
            player.setScore(player.getScore() + activePoints);
            // Set locked so the state does not look odd for the next player. These have already been totaled and will
            // be reset on a new turn anyway
            for (Die die : dice)
                if (die.isSelected()) {
                    die.setLocked(true);
                }
        }
        else {
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.NOT_ENOUGH_POINTS,
                    getActivePlayer());
            mutex.unlock();
            return;
        }
        if (activePoints + rollPoints >= SCORE_START_THRESHOLD)
            sendAchievement(AchievementHandler.Achievement.FIRST_SCORE, getActivePlayer());
        handleZilch(false);
        setNextPlayerActive();
        sendActivePoints(activePoints);
        sendPlayerUpdate(players);
        sendDieUpdate(dice);
        mutex.unlock();
    }

    @Override
    public void stop() {
        handleActions = false;
    }

    @Override
    public void act(float delta) {
        if (!handleActions) {
            return;
        }
        mutex.lock();
        // AI logic
        int active = getActivePlayer();
        if (active == -1) {
            mutex.unlock();
            return;
        }
        Player player = players.get(active);
        if (!player.isBot()) {
            mutex.unlock();
            return;
        }
        int threshold = 0;
        if (player.getBotType() != null)
            threshold = player.getBotType().stopThreshold;
        boolean avoidZilch = player.getZilchAmount() == ZILCH_MAX &&
                (player.getBotType().avoidsZilch || random.nextFloat() < .5);
        if (!turnStarted)
            rollClicked();
        else {
            boolean clickedOneOrFive = false;
            for (Die die : dice) {
                if ((die.getFace() == 1 || die.getFace() == 5) && !die.isLocked() &&
                        !die.isSelected()) {
                    clickDie(dice.indexOf(die));
                    clickedOneOrFive = true;
                }
            }
            int matches = 0;
            int found = 0;
            if (!clickedOneOrFive) {
                face:
                for (int face = 1; face <= 6; face++) {
                    for (Die die : dice) {
                        if (die.getFace() == face && !die.isLocked())
                            matches++;
                        if (dice.size() == dice.indexOf(die) + 1 && matches >= 3) {
                            found = face;
                            break face;
                        }
                    }
                }
            }
            if (found > 0)
                for (Die die : dice)
                    if (die.getFace() == found && !die.isLocked() && !die.isSelected())
                        clickDie(dice.indexOf(die));
            ArrayList<Integer> selected = new ArrayList<Integer>();
            for (Die die : dice)
                if (die.isSelected())
                    selected.add(die.getFace());
            int points = scoreSelected(selected) + activePoints;
            // Check max score if last turn is enabled
            boolean hasMaxScore = true;
            for (Player p : players)
                if (p != player && p.getScore() >= player.getScore())
                    hasMaxScore = false;
            // Check thresholds
            boolean isBelowInitialThreshold =
                    player.getScore() < SCORE_START_THRESHOLD && points < SCORE_START_THRESHOLD;
            boolean isBelowBotThreshold = points < threshold;
            boolean isBelowLastRoundThreshold = (lastRound && !hasMaxScore);
            if (!isBelowInitialThreshold && !isBelowLastRoundThreshold && player.getZilchAmount() == ZILCH_MAX &&
                    avoidZilch && points > 0)
                stopClicked();
            if (isBelowInitialThreshold || isBelowBotThreshold || isBelowLastRoundThreshold)
                rollClicked();
            else
                stopClicked();
        }
        mutex.unlock();
    }

    @Override
    public void loadState(String saveJson) throws GreedyException {
        Gson gson = new Gson();
        JsonObject save = gson.fromJson(saveJson, JsonObject.class);
        int apiVersion = save.get("apiVersion").getAsInt();
        if (apiVersion != Constants.API_VERSION)
            throw new GreedyException("API version mismatch");
        players = gson.fromJson(save.get("players").getAsString(), new TypeToken<ArrayList<Player>>(){}.getType());
        if (singlePlayer) {
            players.get(0).setName(PreferencesUtil.getPlayerName());
            players.get(0).setIcon(PreferencesUtil.getIcon());
        }
        dice = gson.fromJson(save.get("dice").getAsString(), new TypeToken<ArrayList<Die>>(){}.getType());
        turnStarted = save.get("turnStarted").getAsBoolean();
        activePoints = save.get("activePoints").getAsInt();
        lastRound = save.get("lastRound").getAsBoolean();
        lastRoundStarter = save.get("lastRoundStarter").getAsInt();
        handleActions = save.get("handleActions").getAsBoolean();
        numberOfPlayers = save.get("numberOfPlayers").getAsInt();
        gameType = GameType.values()[save.get("gameType").getAsInt()];
        enableBots = save.get("enableBots").getAsBoolean();
        singlePlayer = save.get("singlePlayer").getAsBoolean();
        gameOver = save.get("gameOver").getAsBoolean();
    }

    @Override
    public String saveState() {
        Gson gson = new Gson();
        JsonObject save = new JsonObject();
        save.addProperty("apiVersion", Constants.API_VERSION);
        save.addProperty("players", gson.toJson(players));
        save.addProperty("dice", gson.toJson(dice));
        save.addProperty("turnStarted", turnStarted);
        save.addProperty("activePoints", activePoints);
        save.addProperty("lastRound", lastRound);
        save.addProperty("lastRoundStarter", lastRoundStarter);
        save.addProperty("handleActions", handleActions);
        save.addProperty("numberOfPlayers", numberOfPlayers);
        save.addProperty("gameType", gameType.ordinal());
        save.addProperty("enableBots", enableBots);
        save.addProperty("singlePlayer", singlePlayer);
        save.addProperty("gameOver", gameOver);
        return gson.toJson(save);
    }

    private int scoreSelected(ArrayList<Integer> selected) {
        int score = 0;
        // Calculate three or more and pairs
        int currentFaceAmount = 0;
        ArrayList<int[]> threeOrMore = new ArrayList<int[]>(); // [face, amount]
        ArrayList<Integer> pairs = new ArrayList<Integer>();
        ArrayList<Integer> solos = new ArrayList<Integer>();
        for (int face = 0; face < 6; face++) {
            for (int selection = 0; selection < selected.size(); selection++) {
                if (selected.get(selection) == face + 1)
                    currentFaceAmount++;
                if (selection == selected.size() - 1) {
                    if (currentFaceAmount >= 3)
                        threeOrMore.add(new int[] {face + 1, currentFaceAmount});
                    else if (currentFaceAmount == 2)
                        pairs.add(face + 1);
                    else if (currentFaceAmount == 1)
                        solos.add(face + 1);
                }
            }
            currentFaceAmount = 0;
        }

        // Check if an invalid value has been selected
        if (threeOrMore.size() == 0) {
            // Pairs contain a invalid option
            for (int jerry : pairs)
                if (jerry != 1 && jerry != 5)
                    return 0;
        }
        // Solos have an invalid option
        for (int solo : solos)
            if (solo != 1 && solo != 5)
                return 0;

        // Add three or mores to score
        for (int[] tom : threeOrMore)
            score += SCORE_THREE_OR_MORE[tom[0] - 1] * (tom[1] - 2);
        // Add pairs if there is a three of a kind
        if (threeOrMore.size() > 0)
            for (int jerry : pairs)
                score += SCORE_PAIR[jerry - 1] * 2;
        // Add duo 1/5 if there is no three of a kind
        else {
            for (int jerry : pairs)
                if (jerry == 1)
                    score += SCORE_ONE * 2;
                else if (jerry == 5)
                    score += SCORE_FIVE * 2;
        }
        // Add solo 1/5
        ArrayList<Integer> oneFive = new ArrayList<Integer>();
        for (int[] tom : threeOrMore)
            if (tom[0] == 1 || tom[0] == 5)
                oneFive.add(tom[0]);
        if (!oneFive.contains(1) && !pairs.contains(1) && solos.contains(1))
            score += SCORE_ONE;
        if (!oneFive.contains(5) && !pairs.contains(5) && solos.contains(5))
            score += SCORE_FIVE;
        return score;
    }

    /**
     * Ends the turn and sends a turn end notification.
     * Deactivates the current player and activates the next.
     * Checks win condition and starts a last round.
     */
    private void setNextPlayerActive() {
        int currentPlayer = getActivePlayer();
        // Send an end turn update
        sendTurnEnd(currentPlayer, activePoints);
        // Check win condition
        for (Player player : players) {
            // Player has hit the win threshold. Start the last round
            if (player.getScore() >= SCORE_WIN && !lastRound) {
                lastRound = true;
                lastRoundStarter = players.indexOf(player);
                sendLastRound(lastRoundStarter);
                break;
            }
        }
        // Deactivate current player and set the next active
        activePoints = 0;
        turnStarted = false;
        players.get(currentPlayer).setActive(false);
        if (players.size() > ++currentPlayer)
            players.get(currentPlayer).setActive(true);
        else {
            currentPlayer = 0;
            players.get(currentPlayer).setActive(true);
        }
        // Last round has ended. Check the scores for the highest amount.
        if (lastRound && lastRoundStarter == currentPlayer) {
            lastRound = false;
            handleActions = false;
            sendPlayerUpdate(players);
            calculateAndSendWinner();
        }
        // Zilch warning
        else if (players.get(currentPlayer).getZilchAmount() == ZILCH_MAX)
            sendZilchWarning(getActivePlayer());
    }

    /**
     * Forces the current player to zilch then sets the next one as the active player.
     */
    public void forceNextPlayerActive() {
        activePoints = 0;
        sendAchievement(AchievementHandler.Achievement.TIME_UP, getActivePlayer());
        sendFailUpdate(ControlEventListener.Action.TURN, ControlEventListener.FailReason.TIME_UP, getActivePlayer());
        handleZilch(true);
        setNextPlayerActive();
        sendPlayerUpdate(players);
    }

    private void calculateAndSendWinner() {
        // Order players
        Player[] order = new Player[players.size()];
        for (int playerOrder = 0; playerOrder < players.size(); playerOrder++) {
            int[] largestScore = new int[] {-1, -1}; // player index, score
            for (Player player : players) {
                if (player.getScore() > largestScore[1] && !contains(order, player)) {
                    largestScore[0] = players.indexOf(player);
                    largestScore[1] = player.getScore();
                }
            }
            order[playerOrder] = players.get(largestScore[0]);
        }
        // Clear active players
        for (Player player : order)
            player.setActive(false);
        order[0].setActive(true);
        sendAchievement(AchievementHandler.Achievement.WIN, players.indexOf(order[0]));
        if (players.indexOf(order[0]) != lastRoundStarter)
            sendAchievement(AchievementHandler.Achievement.WIN_LAST_ROUND, players.indexOf(order[0]));
        gameOver = true;
        sendWinner(new ArrayList<Player>(Arrays.asList(order)));
    }

    private boolean contains(Object[] objs, Object o) {
        for (Object oo : objs)
            if (oo != null && oo.equals(o))
                return true;
        return false;
    }

    private boolean diceHavePlayableValue() {
        // One or five
        for (Die die : dice)
            if ((die.getFace() == 5 || die.getFace() == 1) && !die.isLocked() && !die.isSelected())
                return true;
        // 3 of a kind or more
        int faceCount = 0;
        for (int faceNum = 1; faceNum <= 6; faceNum++) {
            for (Die die : dice)
                if (die.getFace() == faceNum && !die.isLocked() && !die.isSelected())
                    faceCount++;
            if (faceCount >= 3)
                return true;
            faceCount = 0;
        }
        return false;
    }

    private void populatePlayers() {
        for (int playerNum = 0; playerNum < numberOfPlayers; playerNum++) {
            Player human = new Player();
            if (singlePlayer && playerNum == 0) {
                human.setName(PreferencesUtil.getPlayerName());
                human.setIcon(PreferencesUtil.getIcon());
            }
            if (playerNum == 0)
                human.setActive(true);
            if (getNames().size() > playerNum)
                human.setName(getNames().get(playerNum));
            if (getIcons().size() > playerNum)
                human.setIcon(getIcons().get(playerNum));
            players.add(human);
        }
        if (!enableBots)
            return;
        for (int playerNum = numberOfPlayers; playerNum < Constants.MAX_PLAYERS; playerNum++) {
            Player bot = new Player();
            bot.setBot(true);
            bot.setBotType(Player.BotType.values()[random.nextInt(Player.BotType.values().length)]);
            bot.setName(bot.getBotType().username);
            bot.setIcon(Icon.values()[random.nextInt(Icon.values().length)]);
            players.add(bot);
        }
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public void setIcons(ArrayList<Icon> icons) {
        this.icons = icons;
    }

    public ArrayList<Icon> getIcons() {
        return icons;
    }
}
