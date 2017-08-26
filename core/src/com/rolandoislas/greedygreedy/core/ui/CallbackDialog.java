package com.rolandoislas.greedygreedy.core.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;

public class CallbackDialog extends Dialog {
    public CallbackDialog(String title, Skin skin) {
        super(title, skin);
        this.getTitleTable().setSkin(skin);
        this.getTitleTable().padBottom(skin.get("default", Label.LabelStyle.class).font.getLineHeight() * 2);
    }

    @Override
    protected void result(Object object) {
        if (getStage() instanceof DialogCallbackHandler)
            ((DialogCallbackHandler)getStage()).dialogResult(object);
        hide(null);
    }
}
