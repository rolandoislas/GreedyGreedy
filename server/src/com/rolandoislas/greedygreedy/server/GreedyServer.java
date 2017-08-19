package com.rolandoislas.greedygreedy.server;

import com.rolandoislas.greedygreedy.core.util.AiController;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rolando on 7/13/17.
 */
public class GreedyServer extends WebSocketServer {
    private static Logger logger;

    private GreedyServer(InetSocketAddress address) {
        super(address);
    }

    public static void main(String[] args) {
        AiController aicontroller = new AiController();
        aicontroller.start();
        logger = Logger.getLogger("SERVER");
        logger.setLevel(Level.ALL); // TODO set log level via command line args
        int port = 5000;
        try {
            if (System.getenv("PORT") != null && !System.getenv("PORT").isEmpty())
            port = Integer.parseInt(System.getenv("PORT"));
        }
        catch (NumberFormatException e) {
            logger.warning(String.format("Failed to parse port: %s", System.getenv("PORT")));
        }
        GreedyServer server = new GreedyServer(new InetSocketAddress(port));
        server.start();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        conn.send(message);
        logger.info(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        logger.info(String.format(Locale.US,"Server running on port %d", getPort()));
    }
}
