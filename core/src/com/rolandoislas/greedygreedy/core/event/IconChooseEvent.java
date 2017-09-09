package com.rolandoislas.greedygreedy.core.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.rolandoislas.greedygreedy.core.data.Purchase;

public class IconChooseEvent extends Event {
    private int id;
    private Purchase.Type type;
    private int price;
    private boolean owned;

    public IconChooseEvent(int id, Purchase.Type type, int price, boolean owned) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.owned = owned;
    }

    public int getId() {
        return id;
    }

    public Purchase.Type getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public boolean isOwned() {
        return owned;
    }
}
