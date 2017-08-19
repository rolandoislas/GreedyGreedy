package com.rolandoislas.greedygreedy.desktop.net;

import com.rolandoislas.greedygreedy.core.util.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by rolando on 7/15/17.
 */
public class WebClient extends WebSocketClient {
    public WebClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Logger.info("Opened");
        send("test");
    }

    @Override
    public void onMessage(String message) {
        send(message);
        Logger.info(message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.info("Closed");
    }

    @Override
    public void onError(Exception ex) {
        Logger.info(ex.toString());
    }
}
