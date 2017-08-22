package com.rolandoislas.greedygreedy.core.ui.skin;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

public class DialogSkin extends Skin {
    public DialogSkin() {
        // Window
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = TextUtil.generateScaledFont(0.5f);
        add("windowBackground", new Texture("image/log_background.png"));
        windowStyle.stageBackground = getDrawable("windowBackground");
        // Text button
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = TextUtil.generateScaledFont(0.5f);
        // Label style
        Label.LabelStyle lbs = new Label.LabelStyle();
        lbs.font = TextUtil.generateScaledFont(0.5f);
        // Table
        add("default", windowStyle);
        add("default", tbs);
        add("default", lbs);
    }
}
