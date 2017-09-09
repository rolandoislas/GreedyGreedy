package com.rolandoislas.greedygreedy.core.util;

import com.rolandoislas.greedygreedy.core.data.Die;
import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;

import java.util.ArrayList;

public abstract class GameControllerBase implements GameController {
    private ArrayList<ControlEventListener> eventListeners = new ArrayList<ControlEventListener>();

    @Override
    public void addListener(ControlEventListener controlEventListener) {
        eventListeners.add(controlEventListener);
    }

    public void sendWinner(ArrayList<Player> orderedPlayers) {
        for (ControlEventListener listener : eventListeners)
            listener.gameEnd(new ArrayList<Player>(orderedPlayers));
    }

    public void sendLastRound(int lastRoundStarter) {
        for (ControlEventListener listener : eventListeners)
            listener.lastRound(lastRoundStarter);
    }

    public void sendTurnEnd(int player, int points) {
        for (ControlEventListener listener : eventListeners)
            listener.turnEnd(player, points);
    }

    public void sendActivePoints(int activePoints) {
        for (ControlEventListener listener : eventListeners)
            listener.activePoints(activePoints);
    }

    public void sendFailUpdate(final ControlEventListener.Action action,
                               final ControlEventListener.FailReason failReason, int player) {
        for (ControlEventListener listener : eventListeners)
            listener.actionFailed(action, failReason, player);
    }

    public void sendPlayerUpdate(ArrayList<Player> players) {
        for (ControlEventListener listener : eventListeners)
            listener.playerUpdate(new ArrayList<Player>(players));
    }

    public void sendDieUpdate(ArrayList<Die> dice) {
        for (ControlEventListener listener : eventListeners)
            listener.dieUpdate(new ArrayList<IDie>(dice));
    }

    public void sendZilchWarning(int player) {
        for (ControlEventListener listener : eventListeners)
            listener.zilchWarning(player);
    }

    public void sendAchievement(AchievementHandler.Achievement achievement, int player) {
        for (ControlEventListener listener : eventListeners)
            listener.achievement(achievement, player);
    }

    public void sendWhoami(int whoami) {
        for (ControlEventListener listener : eventListeners)
            listener.whoami(whoami);
    }

    public void sendConnected() {
        for (ControlEventListener listener : eventListeners)
            listener.connected();
    }

    public void sendDisconnected() {
        for (ControlEventListener listener : eventListeners)
            listener.disConnected();
    }

    public void sendCountdown(long milliseconds) {
        for (ControlEventListener listener : eventListeners)
            listener.countdown(milliseconds);
    }

    public void sendRollSuccess(int player) {
        for (ControlEventListener listener : eventListeners)
            listener.rollSuccess(player);
    }
}
