package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.actor.Chip;
import com.rolandoislas.greedygreedy.core.actor.PlayerInfoCard;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

import java.util.ArrayList;
import java.util.Locale;

public class StagePostGame extends Stage {
    private final ArrayList<PlayerInfoCard> playerInfoCards;
    private final Chip chip;

    public StagePostGame(ArrayList<Player> players, int whoami, boolean gameGivesPoints) {
        // Player list
        playerInfoCards = new ArrayList<PlayerInfoCard>();
        for (Player player : players) {
            PlayerInfoCard pic = new PlayerInfoCard();
            float y = getHeight() - getHeight() * .1f * (players.indexOf(player) + 1);
            pic.setBounds(0, y, getWidth(), getHeight() * .1f);
            pic.setStats(player);
            playerInfoCards.add(pic);
            addActor(pic);
        }
        // New points
        Label.LabelStyle lbs = new Label.LabelStyle();
        lbs.font = TextUtil.generateScaledFont(1f);
        int points = 0;
        if (gameGivesPoints)
            points = Constants.WINNING_POINTS_DISTRIBUTIONS[whoami];
        Label pointsLabel = new Label(String.format(Locale.US, "+%d %s",
                points,
                points == 1 ? "chip" : "chips"), lbs);
        pointsLabel.setPosition(getWidth() / 2 - pointsLabel.getWidth() / 2,
                playerInfoCards.get(playerInfoCards.size() - 1).getY() - pointsLabel.getHeight() -
                        getHeight() * .025f);
        addActor(pointsLabel);
        // Chip icon
        chip = new Chip();
        chip.setColor(Color.BLACK);
        chip.setSize(getWidth() * .25f, getWidth() * .25f);
        chip.setPosition((getWidth() - chip.getWidth()) / 2, pointsLabel.getY() - chip.getHeight());
        chip.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageLogin(new StageStore(), null));
            }
        });
        addActor(chip);
        // Chip points overlay
        Label.LabelStyle chipOverlayStyle = new Label.LabelStyle();
        chipOverlayStyle.font = TextUtil.generateScaledFont(.5f);
        PreferencesUtil.addPoints(points);
        PreferencesUtil.updatePointsAsync();
        Label chipOverlay = new Label(String.valueOf(PreferencesUtil.getPoints()), chipOverlayStyle);
        chipOverlay.setPosition(chip.getX() + chip.getWidth() / 2 - chipOverlay.getWidth() / 2,
                chip.getY() + chip.getHeight() / 2 - chipOverlay.getHeight() / 2);
        chipOverlay.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageLogin(new StageStore(), null));
            }
        });
        addActor(chipOverlay);
        // Home button
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(1f);
        final TextButton startGame = new TextButton("Main Menu", tbs);
        startGame.setPosition(getWidth() / 2 - startGame.getWidth() / 2, 0);
        startGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GreedyClient.setStage(new StageMenu());
            }
        });
        addActor(startGame);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (PlayerInfoCard pic : playerInfoCards)
            pic.dispose();
        chip.dispose();
    }

    @Override
    public void onBackButtonPressed() {
        GreedyClient.setStage(new StageMenu());
    }

    @Override
    public Color getBackgroundColor() {
        return Constants.COLOR_YELLOW;
    }
}
