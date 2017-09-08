package com.rolandoislas.greedygreedy.core.util;

public class GameOptionsUtil {
    public static GameOptionsUtil.PointValue parseOptions(int players, boolean enableBots, boolean privateGame,
                                                          GameController.GameType gameType) {
        if (players < 4 && !enableBots)
            return PointValue.NOT_ENOUGH_PLAYERS;
        if (privateGame)
            return PointValue.PRIVATE_GAME;
        return PointValue.FULL_POINTS;
    }

    public enum PointValue {PRIVATE_GAME, FULL_POINTS, NOT_ENOUGH_PLAYERS}
}
