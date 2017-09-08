package com.rolandoislas.greedygreedy.core.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.rolandoislas.greedygreedy.core.event.DialogCallbackHandler;

public class CallbackDialog extends Dialog {
    public CallbackDialog(String title, Skin skin) {
        super(title, skin);
        this.getTitleTable().setSkin(skin);
        this.getTitleTable().padBottom(skin.get("default", Label.LabelStyle.class).font.getLineHeight() * 2);
        this.getTitleTable().align(Align.center);
        for (Cell cell : this.getTitleTable().getCells())
            if (cell.getActor() instanceof Label)
                ((Label) cell.getActor()).setAlignment(Align.center);
    }

    @Override
    protected void result(Object object) {
        if (getStage() instanceof DialogCallbackHandler)
            ((DialogCallbackHandler)getStage()).dialogResult(object);
        hide(null);
    }

    @Override
    public Dialog show(Stage stage) {
        show(stage, null);
        setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        return this;
    }
}
