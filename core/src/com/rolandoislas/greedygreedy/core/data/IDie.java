package com.rolandoislas.greedygreedy.core.data;

public interface IDie {
    void setStats(IDie die);

    void setLocked(boolean locked);

    boolean isLocked();

    void setSelected(boolean selected);

    boolean isSelected();

    int getFace();

    void setFace(int faceValue);
}
