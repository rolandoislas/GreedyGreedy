package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

public class PlayerInfoCard extends Actor {
    private final ShapeRenderer shapeRenderer;
    private final Label name;
    private final Label score;
    private final Texture image;
    private final Label tempScore;
    private boolean empty;
    private boolean active;
    private boolean resizeName;

    public PlayerInfoCard() {
        empty = true;
        shapeRenderer =  new ShapeRenderer();
        // Image
        image = new Texture("image/icon_512.png"); // TODO add actual icons
        // Text
        Label.LabelStyle lbs = new Label.LabelStyle();
        lbs.font = TextUtil.generateScaledFont(0.25f);
        name = new Label("Open", lbs);
        score = new Label("", lbs);
        Label.LabelStyle tlbs = new Label.LabelStyle();
        tlbs.font = TextUtil.generateScaledFont(0.25f);
        tlbs.fontColor = Constants.COLOR_RED;
        tempScore = new Label("", tlbs);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(active ? Constants.COLOR_GREEN_DARK : Color.DARK_GRAY);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();
        batch.begin();
        if (!empty)
            batch.draw(image, getX() + getHeight() * .1f, getY() + getHeight() * .1f,
                    getHeight() * .8f, getHeight() * .8f);
        name.draw(batch, parentAlpha);
        score.draw(batch, parentAlpha);
        tempScore.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta) {
        if (resizeName) {
            GlyphLayout layout = new GlyphLayout();
            layout.setText(name.getStyle().font, name.getText().toString());
            while (layout.width > getWidth() - getHeight() && name.getText().length > 2) {
                name.setText(name.getText().substring(0, name.getText().length - 2));
                layout.setText(name.getStyle().font, name.getText().toString());
            }
            resizeName = false;
        }
    }

    @Override
    protected void positionChanged() {
        if (empty)
            name.setPosition(getX() + getWidth() / 2 - name.getWidth() / 2,
                    getY() + getHeight() / 2 - name.getHeight() / 2);
        else
            name.setPosition(getX() + getHeight(), getY() + getHeight() - name.getHeight() - getHeight() * .15f);
        score.setPosition(name.getX(), name.getY() - name.getHeight());
        tempScore.setPosition(score.getX(), score.getY() - name.getHeight());
    }

    @Override
    protected void sizeChanged() {
        positionChanged();
    }

    @Override
    public void setName(String name) { // TODO size the names better
        super.setName(name);
        this.name.setText(name);
        this.resizeName = true;
        this.empty = false;
        sizeChanged();
    }

    public void setScore(int score) {
        this.score.setText(String.valueOf(score));
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active)
            this.tempScore.setText("");
    }

    public void setTempScore(int tempScore) {
        if (tempScore == 0)
            this.tempScore.setText("");
        else
            this.tempScore.setText(String.valueOf(tempScore));
    }

    public void setStats(Player player) {
        this.setName(player.getName());
        this.setScore(player.getScore());
        this.setActive(player.isActive());
    }
}
