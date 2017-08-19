package com.rolandoislas.greedygreedy.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Die;
import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class AiController implements GameController {
    private static final int[] SCORE_THREE_OR_MORE = new int[] {1000, 200, 300, 400, 500, 600};
    private static final int SCORE_ONE = 100;
    private static final int SCORE_FIVE = 50;
    private static final int[] SCORE_PAIR = new int[] {200, 50, 50, 50, 100, 50};
    private static final int SCORE_START_THRESHOLD = 1000;
    private static final int SCORE_WIN = 10000;
    private ArrayList<ControlEventListener> eventListeners = new ArrayList<ControlEventListener>();
    private ArrayList<Player> players = new ArrayList<Player>();
    private Random random = new Random();
    private ArrayList<Die> dice = new ArrayList<Die>();
    private boolean turnStarted;
    private int activePoints;
    private boolean lastRound;
    private int lastRoundStarter;
    private boolean handleActions;
    private ReentrantLock mutex = new ReentrantLock();
    private float aiTime;

    @Override
    public void addListener(ControlEventListener controlEventListener) {
        eventListeners.add(controlEventListener);
    }

    @Override
    public void start() {
        for (ControlEventListener listener :
                eventListeners) {
            listener.connected();
            listener.whoami(0);
        }
        if (players.size() == 0)
            populatePlayers();
        if (dice.size() == 0)
            resetDice();
        sendPlayerUpdate();
        handleActions = true;
        // Send the state
        sendPlayerUpdate();
        sendDieUpdate();
        sendActivePoints();
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
            sendFailUpdate(ControlEventListener.Action.DIE, ControlEventListener.FailReason.TURN_NOT_STARTED);
            mutex.unlock();
            return;
        }
        Die die = dice.get(dieNum);
        if (!die.isLocked()) {
            if (die.isSelected()) {
                die.setSelected(false);
            }
            else {
                die.setSelected(true);
            }
        }
        sendDieUpdate();
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
                GreedyClient.achievementHandler.give(AchievementHandler.Achievement.INSTANT_FAIL);
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_PLAYABLE_VALUES);
                setNextPlayerActive();
                sendPlayerUpdate();
            }
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
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_SELECTION);
                mutex.unlock();
                return;
            }
            // Calculate roll score
            int rollScore = scoreSelected(selected);
            if (rollScore > 0) {
                activePoints += rollScore;
            }
            else {
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_SCORE);
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
                resetDice();
                for (Die die : dice)
                    die.setFace(random.nextInt(6) + 1);
            }
            // Check the post roll for playable values
            if (!diceHavePlayableValue()) {
                sendFailUpdate(ControlEventListener.Action.ROLL, ControlEventListener.FailReason.NO_PLAYABLE_VALUES);
                activePoints = 0;
                setNextPlayerActive();
            }
            sendActivePoints();
            sendPlayerUpdate();
        }
        sendDieUpdate();
        mutex.unlock();
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
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.TURN_NOT_STARTED);
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
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.NO_SELECTION);
            mutex.unlock();
            return;
        }
        // Check player is above point threshold
        Player player = players.get(getActivePlayer());
        boolean hasMaxScore = true;
        for (Player p : players)
            if (p != player && p.getScore() >= player.getScore())
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
            sendFailUpdate(ControlEventListener.Action.STOP, ControlEventListener.FailReason.NOT_ENOUGH_POINTS);
            mutex.unlock();
            return;
        }
        setNextPlayerActive();
        sendActivePoints();
        sendPlayerUpdate();
        sendDieUpdate();
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
        if (aiTime < 1) {
            aiTime += delta;
            //return; // TODO instant bot option
        }
        aiTime = 0;
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
        //boolean avoidZilch = player.getBotType().avoidsZilch; // TODO avoid ziltch
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
            if ((player.getScore() < SCORE_START_THRESHOLD && points < SCORE_START_THRESHOLD) ||
                    points < threshold || (lastRound && !hasMaxScore))
                rollClicked();
            else
                stopClicked();
        }
        mutex.unlock();
    }

    @Override
    public void loadState(String saveJson) {
        Gson gson = new Gson();
        JsonObject save = gson.fromJson(saveJson, JsonObject.class);
        players = gson.fromJson(save.get("players").getAsString(), new TypeToken<ArrayList<Player>>(){}.getType());
        dice = gson.fromJson(save.get("dice").getAsString(), new TypeToken<ArrayList<Die>>(){}.getType());
        turnStarted = save.get("turnStarted").getAsBoolean();
        activePoints = save.get("activePoints").getAsInt();
        lastRound = save.get("lastRound").getAsBoolean();
        lastRoundStarter = save.get("lastRoundStarter").getAsInt();
        handleActions = save.get("handleActions").getAsBoolean();
    }

    @Override
    public String saveState() {
        Gson gson = new Gson();
        JsonObject save = new JsonObject();
        save.addProperty("players", gson.toJson(players));
        save.addProperty("dice", gson.toJson(dice));
        save.addProperty("turnStarted", turnStarted);
        save.addProperty("activePoints", activePoints);
        save.addProperty("lastRound", lastRound);
        save.addProperty("lastRoundStarter", lastRoundStarter);
        save.addProperty("handleActions", handleActions);
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
                sendLastRound();
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
            sendPlayerUpdate();
            sendWinner();
        }
    }

    private void sendLastRound() {
        for (ControlEventListener listener : eventListeners)
            listener.lastRound(lastRoundStarter);
    }

    private void sendWinner() {
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
        for (ControlEventListener listener : eventListeners)
            listener.gameEnd(new ArrayList<Player>(Arrays.asList(order)));
    }

    private boolean contains(Object[] objs, Object o) {
        for (Object oo : objs)
            if (oo != null && oo.equals(o))
                return true;
        return false;
    }

    private void sendTurnEnd(int player, int points) {
        for (ControlEventListener listener : eventListeners)
            listener.turnEnd(player, points);
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
        Player human = new Player();
        human.setName(PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL).getString(Constants.PREF_USERNAME));
        human.setActive(true);
        players.add(human);
        for (int playerNum = 0; playerNum < Constants.MAX_PLAYERS - 1; playerNum++) {
            Player bot = new Player();
            bot.setBot(true);
            bot.setBotType(Player.BotType.values()[random.nextInt(Player.BotType.values().length)]);
            bot.setName(bot.getBotType().username);
            players.add(bot);
        }
    }

    private void sendActivePoints() {
        for (ControlEventListener listener : eventListeners)
            listener.activePoints(activePoints);
    }

    private void sendFailUpdate(final ControlEventListener.Action action, final ControlEventListener.FailReason failReason) {
        for (ControlEventListener listener : eventListeners)
            listener.actionFailed(action, failReason);
    }

    private void sendPlayerUpdate() {
        for (ControlEventListener listener : eventListeners)
            listener.playerUpdate(new ArrayList<Player>(players));
    }

    private void sendDieUpdate() {
        for (ControlEventListener listener : eventListeners)
            listener.dieUpdate(new ArrayList<IDie>(dice));
    }
}
