package com.rolandoislas.greedygreedy.server;

import com.rolandoislas.greedygreedy.core.util.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class GreedySocketServer {
    @OnWebSocketConnect
    public void onConnect(Session socket) throws Exception {

    }

    @OnWebSocketClose
    public void onClose(Session socket, int statusCode, String reason) {

    }

    @OnWebSocketMessage
    public void onMessage(Session socket, String message) {

    }

    @OnWebSocketError
    public void onError(Session socket, Throwable error) {

    }

    private void send(Session socket, String message) {
        try {
            socket.getRemote().sendString(message);
        } catch (IOException e) {
            Logger.exception(e);
        }
    }
}
