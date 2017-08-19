package com.rolandoislas.greedygreedy.core.stage;

import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StagePostGame extends Stage {
    public StagePostGame(ArrayList<Player> players) {
        for (Player player : players) {
            PlayerInfoCard pic = new PlayerInfoCard();
            float y = getHeight() - getHeight() * .1f * (players.indexOf(player) + 1);
            pic.setBounds(0, y, getWidth(), getHeight() * .1f);
            pic.setStats(player);
            addActor(pic);
        }
    }

    @Override
    public void onBackButtonPressed() {
        GreedyClient.setStage(new StageMenu());
    }
}
