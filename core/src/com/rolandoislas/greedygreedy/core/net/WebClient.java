package com.rolandoislas.greedygreedy.core.net;

import com.rolandoislas.greedygreedy.core.event.ControlEventListener;
import com.rolandoislas.greedygreedy.core.util.GameController;

import java.util.ArrayList;

/**
 * Created by rolando on 7/15/17.
 */
public class WebClient implements GameController {
    private ArrayList<ControlEventListener> eventListeners = new ArrayList<ControlEventListener>();

    public WebClient(int numberOfPlayers, boolean privateGame) {

    }

    @Override
    public void addListener(ControlEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public void start() {

    }

    @Override
    public void clickDie(int dieNum) {

    }

    @Override
    public void rollClicked() {

    }

    @Override
    public void stopClicked() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void act(float delta) {

    }

    @Override
    public void loadState(String string) {

    }

    @Override
    public String saveState() {
        return null;
    }
}
