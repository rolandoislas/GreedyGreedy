package com.rolandoislas.greedygreedy.core.util;

public interface AchievementHandler {
    void give(Achievement achievement);

    enum Achievement {ZILCH_RESET, ZILCH, FIRST_SCORE, WIN, ANOTHER_ROLL, WIN_LAST_ROUND, INSTANT_ZILCH}
}
