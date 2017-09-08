package com.rolandoislas.greedygreedy.core.actor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.IDie;

public class DieActor extends Actor implements IDie, Disposable {
    private Texture[] faces;
    private Texture border;
    private ShapeRenderer shapeRenderer;
    private Texture face;
    private float faceSize;
    private float faceOffset;
    private int faceInt;
    private boolean locked;
    private boolean selected;

    public DieActor() {
        this.border = new Texture("image/die_border.png");
        this.shapeRenderer = new ShapeRenderer();
        faces = new Texture[6];
        for (int faceNum = 1; faceNum <= 6; faceNum++)
            faces[faceNum - 1] = new Texture(String.format("image/die_face_%s.png", faceNum));
        setFace(1);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(getColor());
        shapeRenderer.rect(getX() + faceOffset, getY() + faceOffset, faceSize, faceSize);
        shapeRenderer.end();
        batch.begin();
        batch.draw(border, getX(), getY(), getWidth(), getHeight());
        batch.draw(face, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    protected void sizeChanged() {
        faceSize = getWidth() * .98f;
        faceOffset = getWidth() * .02f;
    }

    @Override
    public void setStats(IDie die) {
        this.setLocked(die.isLocked());
        this.setFace(die.getFace());
        this.setSelected(die.isSelected());
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
        applyColor();
    }

    private void applyColor() {
        if (locked)
            setColor(Constants.COLOR_BLUE);
        else if (selected)
            setColor(Constants.COLOR_RED);
        else
            setColor(Constants.COLOR_WHITE);
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        applyColor();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public int getFace() {
        return faceInt;
    }

    @Override
    public void setFace(int faceValue) {
        if (faceValue < 1 || faceValue > 6)
            throw new IllegalArgumentException("Die value out of bounds");
        this.faceInt = faceValue;
        face = faces[faceInt - 1];
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        face.dispose();
        for (Texture faceTexture : faces)
            faceTexture.dispose();
        border.dispose();
    }
}
