package com.rolandoislas.greedygreedy.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import static com.rolandoislas.greedygreedy.server.GreedyServer.gameHandler;

@WebSocket
public class GreedySocketServer {
    private Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session socket) throws Exception {
        synchronized (this) {
            gameHandler.addClient(socket);
        }
    }

    @OnWebSocketClose
    public void onClose(Session socket, int statusCode, String reason) {
        synchronized (this) {
            gameHandler.removeClient(socket);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session socket, String message) {
        synchronized (this) {
            JsonObject json;
            try {
                json = gson.fromJson(message, JsonObject.class);
            } catch (JsonSyntaxException e) {
                return;
            }
            if (!json.has("command"))
                return;
            gameHandler.handleCommand(socket, json);
        }
    }

    @OnWebSocketError
    public void onError(Session socket, Throwable error) {
        synchronized (this) {
            gameHandler.removeClient(socket);
        }
    }

}
