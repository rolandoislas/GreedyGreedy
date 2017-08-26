package com.rolandoislas.greedygreedy.server;

import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.server.util.AuthUtil;
import spark.Filter;

import java.util.logging.Level;

import static spark.Spark.*;

/**
 * Created by rolando on 7/13/17.
 */
public class GreedyServer {
    public static void main(String[] args) {
        Logger.setLevel(Level.ALL); // TODO set level based on args
        // Parse port
        int port = 5000;
        try {
            if (System.getenv("PORT") != null && !System.getenv("PORT").isEmpty())
            port = Integer.parseInt(System.getenv("PORT"));
        }
        catch (NumberFormatException e) {
            Logger.warn(String.format("Failed to parse port: %s", System.getenv("PORT")));
        }
        // Set values
        port(port);
        staticFiles.location("/static/");
        // Web sockets
        webSocket("/game", GreedySocketServer.class);
        // Redirect paths with a trailing slash
        before((Filter) (request, response) -> {
            if (request.pathInfo().endsWith("/") && !request.pathInfo().equals("/"))
                response.redirect(request.pathInfo().substring(0, request.pathInfo().length() - 1));
        });
        // API
        path("/api", () -> {
            before("/*", (request, response) -> response.type("application/json"));
            path("/auth", () -> {
                get("/callback", GreedySparkServer::handleAuthCallback);
                get("/token/verify", GreedySparkServer::handleAuthTokenVerify);
            });
        });

        Logger.info("Server started on port %d.", port);
    }
}
