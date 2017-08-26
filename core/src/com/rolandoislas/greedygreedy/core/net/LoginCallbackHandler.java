package com.rolandoislas.greedygreedy.core.net;

import com.badlogic.gdx.Gdx;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.stage.StageLoad;
import com.rolandoislas.greedygreedy.core.stage.StageLogin;
import com.rolandoislas.greedygreedy.core.stage.StageMenu;
import com.rolandoislas.greedygreedy.core.util.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class LoginCallbackHandler extends WebSocketServer {

    public LoginCallbackHandler(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
    }

    public void listen() {
        start();
        try {
            Thread.sleep(60 * 5 * 1000);
            stop();
        }
        catch (InterruptedException ignore) {}
        catch (IOException e) {
            Logger.exception(e);
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, final String message) {
        Logger.debug("Client received auth code callback.");
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageLogin(new StageMenu(), new StageMenu(), message));
            }
        });
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GreedyClient.setStage(new StageMenu());
            }
        });
    }

    @Override
    public void onStart() {
        Logger.debug("Client listening for auth code callback.");
    }
}
