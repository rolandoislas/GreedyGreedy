package com.rolandoislas.greedygreedy.server;

import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.server.util.AuthUtil;
import com.rolandoislas.greedygreedy.server.util.DatabaseUtil;
import com.rolandoislas.greedygreedy.server.util.GameHandler;
import com.rolandoislas.greedygreedy.server.util.LogAndDieExceptionHandler;
import org.sql2o.Sql2oException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import spark.Filter;

import java.util.logging.Level;

import static spark.Spark.*;

/**
 * Created by rolando on 7/13/17.
 */
public class GreedyServer {
    public static GameHandler gameHandler;

    public static void main(String[] args) {
        AuthUtil a = new AuthUtil();
        Logger.setLevel(Level.ALL); // TODO set level based on args
        // Parse port
        int port = 5000;
        try {
            if (System.getenv("PORT") != null && !System.getenv("PORT").isEmpty())
                port = Integer.parseInt(System.getenv("PORT"));
        }
        catch (NumberFormatException e) {
            Logger.warn(String.format("Failed to parse PORT env var: %s", System.getenv("PORT")));
        }
        // Redis address
        String redisServer = System.getenv("REDIS_URL");
        // Mysql address
        String mysqlServer = System.getenv("JAWSDB_URL");
        // Set values
        port(port);
        staticFiles.location("/static/");
        GreedyServer.gameHandler = new GameHandler(redisServer, mysqlServer);
        DatabaseUtil.setServer(mysqlServer);
        // Web sockets
        exception(JedisException.class, new LogAndDieExceptionHandler());
        exception(Sql2oException.class, new LogAndDieExceptionHandler());
        webSocket("/socket/game", GreedySocketServer.class);
        // Redirect paths with a trailing slash
        before((Filter) (request, response) -> {
            if (request.pathInfo().endsWith("/") && !request.pathInfo().equals("/"))
                response.redirect(request.pathInfo().substring(0, request.pathInfo().length() - 1));
        });
        // API
        path("/api", () -> {
            before("/*", (request, response) -> response.type("application/json"));
            path("/auth", () -> {
                get("/callback", GreedySparkServerApi::handleAuthCallback);
                get("/token/verify", GreedySparkServerApi::handleAuthTokenVerify);
                get("/userinfo", GreedySparkServerApi::handleUserInfoRequest);
            });
            path("/info", () -> {
                get("/points", GreedySparkServerApi::handlePointsRequest);
                get("/version", GreedySparkServerApi::handleVersionRequest);
                get("/icon", GreedySparkServerApi::handleIconRequest);
                put("/icon", GreedySparkServerApi::handleIconPutRequest);
                path("/purchases", () -> {
                    get("", GreedySparkServerApi::handlePurchasesRequest);
                    get("/all", GreedySparkServerApi::handlePurchasesAllRequest);
                });
            });
            path("/purchase", () -> {
                post("/points", GreedySparkServerApi::handlePurchaseViaPoints);
                post("/iab", GreedySparkServerApi::handlePurchaseViaIab);
            });
        });
        // Index
        get("/", GreedySparkServer::getIndex);
        // Info
        path("/info", () -> {
            get("", GreedySparkServer::getInfoIndex);
            get("/privacy", GreedySparkServer::getInfoPrivacy);
            get("/oss", GreedySparkServer::getInfoOss);
        });
        // Download
        get("/download", GreedySparkServer::getDownloadIndex);
        // Play
        get("/game", GreedySparkServer::getGameIndex);
        // Logic thread
        Thread logicThread = new Thread(() -> {
            while (true) {
                try {
                    gameHandler.run();
                }
                catch (JedisConnectionException e) {
                    Logger.exception(e);
                    Logger.warn("Failed to connect to redis server");
                    System.exit(1);
                }
            }
        });
        logicThread.setName("Game Handler Thread");
        logicThread.start();

        Logger.info("Server started on port %d.", port);
    }
}
