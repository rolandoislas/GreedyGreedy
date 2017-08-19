package com.rolandoislas.greedygreedy.core.event;

import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Player;

import java.util.ArrayList;
import java.util.EventListener;

public interface ControlEventListener extends EventListener {
    void connected();

    void disConnected(String reason);

    void dieUpdate(ArrayList<IDie> dice);

    void playerUpdate(ArrayList<Player> players);

    void whoami(int playerNum);

    void actionFailed(Action action, FailReason failReason);

    void activePoints(int activePoints);

    void turnEnd(int player, int points);

    void gameEnd(ArrayList<Player> players);

    void lastRound(int lastRoundStarter);

    enum Action {ROLL, STOP, DIE}

    enum FailReason {NO_PLAYABLE_VALUES, NO_SELECTION, NO_SCORE, NOT_ENOUGH_POINTS, TURN_NOT_STARTED}
}
