package com.rolandoislas.greedygreedy.core.event;

import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.util.AchievementHandler;

import java.util.ArrayList;
import java.util.EventListener;

public interface ControlEventListener extends EventListener {
    void connected();

    void disConnected();

    void dieUpdate(ArrayList<IDie> dice);

    void playerUpdate(ArrayList<Player> players);

    void whoami(int playerNum);

    void actionFailed(Action action, FailReason failReason);

    void activePoints(int activePoints);

    void turnEnd(int player, int points);

    void gameEnd(ArrayList<Player> players);

    void lastRound(int lastRoundStarter);

    void zilchWarning(int player);

    void achievement(AchievementHandler.Achievement achievement, int player);

    void countdown(long milliseconds);

    void rollSuccess();

    enum Action {ROLL, STOP, TURN, DIE}

    enum FailReason {NO_PLAYABLE_VALUES, NO_SELECTION, NO_SCORE, NOT_ENOUGH_POINTS, TIME_UP, TURN_NOT_STARTED}
}
