package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.util.IconUtil;
import com.rolandoislas.greedygreedy.core.util.TextUtil;

public class PlayerInfoCard extends Actor implements Disposable {
    private final ShapeRenderer shapeRenderer;
    private final Label name;
    private final Label score;
    private final Texture gear;
    private Texture image;
    private final Label tempScore;
    private boolean empty;
    private boolean active;
    private boolean resizeName;
    private boolean resizeActor;
    private boolean showGearIcon;
    private Icon currentIcon;

    public PlayerInfoCard() {
        empty = true;
        shapeRenderer =  new ShapeRenderer();
        // Image
        image = new Texture("image/icon_512.png");
        gear = new Texture("image/gear.png");
        // Text
        Label.LabelStyle lbs = new Label.LabelStyle();
        lbs.font = TextUtil.generateScaledFont(0.25f);
        name = new Label("Open", lbs);
        score = new Label("", lbs);
        Label.LabelStyle tlbs = new Label.LabelStyle();
        tlbs.font = TextUtil.generateScaledFont(0.25f);
        tlbs.fontColor = Constants.COLOR_RED;
        tempScore = new Label("", tlbs);
        name.setAlignment(Align.left);
        score.setAlignment(Align.left);
        tempScore.setAlignment(Align.left);
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
        if (!empty) {
            float margin = getHeight() * .1f;
            float size = getHeight() * .8f;
            batch.draw(image, getX() + margin, getY() + margin, size, size);
        }
        name.draw(batch, parentAlpha);
        score.draw(batch, parentAlpha);
        tempScore.draw(batch, parentAlpha);
        if (showGearIcon) {
            float gearSize = getHeight() * .25f;
            batch.draw(gear, getX() + getWidth() - gearSize, getY(), gearSize, gearSize);
        }
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
        if (resizeActor) {
            float freeSpace = (getHeight() - name.getHeight() / 2 * 3);
            float textMargin = freeSpace * .125f;
            freeSpace -= textMargin * 2;
            float topMargin = freeSpace / 2;
            if (empty)
                name.setPosition(getX() + getWidth() / 2 - name.getWidth() / 2,
                        getY() + getHeight() / 2 - name.getHeight() / 2);
            else
                name.setPosition(getX() + getHeight(), getY() + getHeight() - name.getHeight() / 1.5f - topMargin);
            score.setPosition(name.getX(), name.getY() - textMargin);
            tempScore.setPosition(name.getX(), name.getY() - name.getHeight() / 2 - textMargin * 2);
            resizeActor = false;
        }
    }

    @Override
    protected void positionChanged() {
        resizeActor = true;
    }

    @Override
    protected void sizeChanged() {
        resizeActor = true;
    }

    @Override
    public void setName(String name) {
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
        this.setIcon(player.getIcon());
    }

    public void setIcon(Icon icon) {
        if (currentIcon == null || !currentIcon.equals(icon)) {
            currentIcon = icon;
            if (image != null)
                image.dispose();
            image = new Texture(IconUtil.getIconPath(icon));
        }
    }

    public void showGearIcon(boolean showGearIcon) {
        this.showGearIcon = showGearIcon;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        gear.dispose();
        image.dispose();
    }
}
