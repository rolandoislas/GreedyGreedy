package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

public class Chip extends Actor implements Disposable {
    private final ShapeRenderer shapeRenderer;
    private final Texture faceTexture;

    public Chip() {
        shapeRenderer = new ShapeRenderer();
        faceTexture = new Texture("image/chip.png");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float radius = getWidth() / 2f;
        shapeRenderer.circle(getX() + radius, getY() + radius, radius);
        shapeRenderer.end();
        batch.begin();
        batch.setColor(getColor());
        batch.draw(faceTexture, getX(), getY(), getWidth(), getHeight());
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        faceTexture.dispose();
    }
}
