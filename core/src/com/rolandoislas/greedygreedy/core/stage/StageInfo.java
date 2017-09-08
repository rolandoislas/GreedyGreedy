package com.rolandoislas.greedygreedy.core.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

import java.util.Locale;

public class StageInfo extends Stage {
    public StageInfo() {
        // Info
        String info = String.format(Locale.US,
                "%s version %s - API version %d\n\n",
                Constants.NAME,
                Constants.VERSION,
                Constants.API_VERSION);
        // Text Area
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = TextUtil.generateScaledFont(0.25f);
        textFieldStyle.fontColor = Color.WHITE;
        String thirdParty = Gdx.files.internal("text/third-party.txt").readString();
        String license = Gdx.files.internal("text/license.txt").readString().split("-------")[0];
        info += license + "\n" + Constants.GITHUB_REPO + "\n\n--\n\n" + thirdParty;
        TextArea textArea = new TextArea(info, textFieldStyle);
        textArea.setTouchable(Touchable.disabled);
        textArea.setBounds(0, 0, getWidth(), getHeight());
        textArea.setPrefRows(info.split("\n").length);
        // Scroll
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setSize(getWidth(), getHeight() * .7f);
        scrollPane.setPosition(0, getHeight() * .15f);
        scrollPane.setColor(Color.BLUE);
        addActor(scrollPane);
        // Title
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = TextUtil.generateScaledFont(1.25f);
        Label title = new Label(Constants.NAME, ls);
        title.setPosition(getWidth() / 2 - title.getWidth() / 2, getHeight() - title.getHeight());
        addActor(title);
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
    public void onBackButtonPressed() {
        GreedyClient.setStage(new StageMenu());
    }
}
