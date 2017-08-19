package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class HidableLog extends com.badlogic.gdx.scenes.scene2d.ui.List<String> {
    private boolean hidden;
    private float STEP_SIZE = 1250;
    private int maxLines;

    public HidableLog(ListStyle style) {
        super(style);
        super.setVisible(true);
    }

    @Override
    protected void sizeChanged() {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(getStyle().font, "0123456789!aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ");
        maxLines = (int) Math.floor(getHeight() / (layout.height * 1.75f));
    }

    public boolean contains(float x, float y) {
        return x >= getX() && x <= getX() + getWidth() && y >= getY() && y <= getY() + getHeight();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void act(float delta) {
        // Hide
        if (isHidden() && getX() > -getWidth())
            setX(getX() - STEP_SIZE * delta);
        // Show
        else if (!isHidden() && getX() < 0) {
            float x = getX() + STEP_SIZE * delta;
            if (x > 0)
                setX(0);
            else
                setX(x);
        }
    }

    public void add(String message) {
        getItems().add(message);
        while (getItems().size > maxLines)
            getItems().removeIndex(0);
    }
}
