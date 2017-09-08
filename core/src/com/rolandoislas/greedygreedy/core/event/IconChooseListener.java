package com.rolandoislas.greedygreedy.core.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.rolandoislas.greedygreedy.core.data.Purchase;

public class IconChooseListener implements EventListener {
    @Override
    public boolean handle(Event event) {
        if (!(event instanceof IconChooseEvent))
            return false;
        IconChooseEvent e = (IconChooseEvent) event;
        itemSelected(e.getId(), e.getType(), e.getPrice(), e.isOwned());
        return true;
    }

    public void itemSelected(int id, Purchase.Type type, int price, boolean owned) {

    }
}
