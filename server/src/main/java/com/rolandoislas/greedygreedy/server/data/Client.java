package com.rolandoislas.greedygreedy.server.data;

import com.rolandoislas.greedygreedy.core.util.GameController;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.server.GreedyServer;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Client {
    private final Session socket;
    private String token;
    private int gameSize;
    private boolean privateGame;
    private boolean searching;
    private String privateGameId;
    private GameController.GameType gameType;
    private boolean enableBots;

    public Client(Session socket) {
        this.socket = socket;
    }

    public void send(String message) {
        try {
            socket.getRemote().sendString(message);
        } catch (Exception e) {
            Logger.exception(e);
            GreedyServer.gameHandler.removeClient(getSocket());
        }
    }

    public int getGameSize() {
        return gameSize;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGameSize(int gameSize) {
        this.gameSize = gameSize;
    }

    public void setPrivateGame(boolean privateGame) {
        this.privateGame = privateGame;
    }

    public Session getSocket() {
        return socket;
    }

    public String getToken() {
        return token;
    }

    public boolean isSearching() {
        return searching;
    }

    public boolean isPrivateGame() {
        return privateGame;
    }

    public String getPrivateGameId() {
        return privateGameId;
    }

    public void setGameType(GameController.GameType gameType) {
        this.gameType = gameType;
    }

    public void setEnableBots(boolean enableBots) {
        this.enableBots = enableBots;
    }

    public void setSearching(boolean searching) {
        this.searching = searching;
    }

    public GameController.GameType getGameType() {
        return gameType;
    }

    public boolean areBotsEnabled() {
        return enableBots;
    }
}
