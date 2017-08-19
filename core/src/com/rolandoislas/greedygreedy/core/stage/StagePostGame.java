package com.rolandoislas.greedygreedy.core.stage;

import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Player;

import java.util.ArrayList;

public class StagePostGame extends Stage {
    public StagePostGame(ArrayList<Player> players, int winner) {
        PlayerInfoCard win = new PlayerInfoCard();
        win.setBounds(0, 0, getWidth(), getHeight() * .1f);
        win.setStats(players.get(winner));
        addActor(win);
    }
}
