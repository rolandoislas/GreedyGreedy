package com.rolandoislas.greedygreedy.core.util;

import com.rolandoislas.greedygreedy.core.event.ControlEventListener;

public interface GameController {

    void addListener(ControlEventListener eventListener);

    void start();

    void clickDie(int dieNum);

    void rollClicked();

    void stopClicked();

    void stop();

    void act(float delta);

    void loadState(String string) throws GreedyException;

    String saveState();

    enum GameType {ZILCH, ANY, NORMAL}
}
