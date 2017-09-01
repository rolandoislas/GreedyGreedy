package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Countdown extends Actor {
    private final ShapeRenderer shapeRenderer;
    private long millisecondsTotal;
    private float delta;
    private long millisecondsRemaining;
    private float[] region0;
    private float[] region1;
    private float[] region2;
    private float[] region3;
    private float radius;

    public Countdown() {
        shapeRenderer = new ShapeRenderer();
        region0 = new float[]{0, 0, 0, 0, 0, 0};
        region1 = new float[]{0, 0, 0, 0, 0, 0};
        region2 = new float[]{0, 0, 0, 0, 0, 0};
        region3 = new float[]{0, 0, 0, 0, 0, 0};
    }

    public void setTime(long milliseconds) {
        this.millisecondsTotal = milliseconds;
        this.millisecondsRemaining = milliseconds;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (this.millisecondsTotal <= 0 || this.millisecondsRemaining <= 0)
            return;
        batch.end();

        // Render the background circle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.circle(getX(), getY(), radius);
        shapeRenderer.end();

        // Clear the depth buffer
        Gdx.gl.glClearDepthf(1f);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // Enable depth mask rendering and disable rgb color mask
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glColorMask(false, false, false, false);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the masks
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.triangle(region0[0], region0[1], region0[2], region0[3], region0[4], region0[5]);
        shapeRenderer.triangle(region1[0], region1[1], region1[2], region1[3], region1[4], region1[5]);
        shapeRenderer.triangle(region2[0], region2[1], region2[2], region2[3], region2[4], region2[5]);
        shapeRenderer.triangle(region3[0], region3[1], region3[2], region3[3], region3[4], region3[5]);

        shapeRenderer.flush();

        // Enable rgb color mask rendering and disable depth mask
        Gdx.gl.glDepthFunc(GL20.GL_EQUAL);
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glColorMask(true, true, true, true);

        // Draw the circle
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.circle(getX(), getY(), radius);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch.begin();
    }

    @Override
    protected void sizeChanged() {
        this.radius = getWidth() / 2;
    }

    @Override
    public void act(float delta) {
        if (millisecondsTotal <= 0 || millisecondsRemaining <= 0)
            return;
        this.delta += delta;
        this.millisecondsRemaining -= delta * 1000;
        if (this.delta >= 1) {
            this.delta = 0;
            float percent = millisecondsRemaining / (float)millisecondsTotal;
            float percent0 = 1;
            float percent1 = 1;
            float percent2 = 1;
            float percent3 = 1;
            if (percent <= .25) {
                percent3 = 0;
                percent2 = 0;
                percent1 = 0;
                percent0 = percent / .25f;
            }
            else if (percent <= .5) {
                percent3 = 0;
                percent2 = 0;
                percent1 = (percent - .25f) / .25f;
            }
            else if (percent <= .75) {
                percent3 = 0;
                percent2 = (percent - .5f) / .25f;
            }
            else {
                percent3 = (percent - .75f) / .25f;
            }
            float size = getWidth();
            double degree = (90 + 90 * percent0) * Math.PI / 180f;
            float x = (float) (getX() + size * Math.cos(degree));
            float y = (float) (getY() + size * Math.sin(degree));
            region0 = new float[] {
                    getX(), getY(),
                    x, y,
                    getX(), getY() + size
            };
            degree = (180 + 90 * percent1) * Math.PI / 180f;
            x = (float) (getX() + size * Math.cos(degree));
            y = (float) (getY() + size * Math.sin(degree));
            region1 = new float[] {
                    getX(), getY(),
                    getX() - size, getY(),
                    x, y
            };
            degree = (90 * percent2 - 90) * Math.PI / 180f;
            x = (float) (getX() + size * Math.cos(degree));
            y = (float) (getY() + size * Math.sin(degree));
            region2 = new float[] {
                    getX(), getY(),
                    x, y,
                    getX(), getY() - size
            };
            degree = 90 * percent3 * Math.PI / 180f;
            x = (float) (getX() + size * Math.cos(degree));
            y = (float) (getY() + size * Math.sin(degree));
            region3 = new float[] {
                    getX(), getY(),
                    getX() + size, getY(),
                    x, y,
            };
        }
    }
}
