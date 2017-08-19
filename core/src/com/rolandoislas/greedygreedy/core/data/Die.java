package com.rolandoislas.greedygreedy.core.data;

public class Die implements IDie {
    private int faceInt = 1;
    private boolean locked;
    private boolean selected;

    public void setStats(IDie die) {
        this.setLocked(die.isLocked());
        this.setFace(die.getFace());
        this.setSelected(die.isSelected());
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getFace() {
        return faceInt;
    }

    public void setFace(int faceValue) {
        if (faceValue < 1 || faceValue > 6)
            throw new IllegalArgumentException("Die value out of bounds");
        faceInt = faceValue;
    }
}
