package com.rolandoislas.greedygreedy.core.net;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Die;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;
import com.rolandoislas.greedygreedy.core.util.*;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by rolando on 7/15/17.
 */
public class WebClient extends GameControllerBase implements GameController {
    private WebSocketClient webSocket;
    private final Gson gson;
    private final int numberOfPlayers;
    private final boolean privateGame;
    private final GameType gameType;
    private final boolean enableBots;
    private long pingTime;
    private long PING_INTERVAL = 30000;

    public WebClient(int numberOfPlayers, boolean privateGame, GameType gameType, boolean enableBots) {
        this.numberOfPlayers = numberOfPlayers;
        this.privateGame = privateGame;
        this.gameType = gameType;
        this.enableBots = enableBots;
        gson = new Gson();
    }

    private static URI getWebSocketUrl() {
        try {
            String uri = GreedyApi.getApiUrl().replace("http", "ws");
            return new URI(uri + "socket/game");
        } catch (URISyntaxException e) {
            Logger.exception(e);
            throw new RuntimeException("Zoinks! Like, hey Scoob, the hardcoded(!) web socket URI has a syntax error.");
        }
    }

    @Override
    public void start() {
        webSocket = new WebSocketClient(getWebSocketUrl());

        if (webSocket.getURI().toString().startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                webSocket.setSocket(factory.createSocket());
            } catch (NoSuchAlgorithmException e) {
                Logger.exception(e);
            } catch (KeyManagementException e) {
                Logger.exception(e);
            } catch (IOException e) {
                Logger.exception(e);
            }
        }

        webSocket.connect();
    }

    @Override
    public void clickDie(int dieNum) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", Constants.COMMAND_CLICK_DIE);
        jsonObject.addProperty("dieNum", dieNum);
        webSocket.send(jsonObject.toString());
    }

    @Override
    public void rollClicked() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", Constants.COMMAND_CLICK_ROLL);
        webSocket.send(jsonObject.toString());
    }

    @Override
    public void stopClicked() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", Constants.COMMAND_CLICK_STOP);
        webSocket.send(jsonObject.toString());
    }

    @Override
    public void stop() {
        webSocket.close();
    }

    @Override
    public void act(float delta) {
        if (!webSocket.isOpen())
            return;
        if (System.currentTimeMillis() - pingTime > PING_INTERVAL) {
            webSocket.sendPing();
            pingTime = System.currentTimeMillis();
        }
    }

    @Override
    public void loadState(String string) {
        throw new RuntimeException("Can't load a remote state");
    }

    @Override
    public String saveState() {
        throw new RuntimeException("Can't save a remote state");
    }

    private class WebSocketClient extends org.java_websocket.client.WebSocketClient {
        WebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            // Register as user with token
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("command", Constants.COMMAND_REGISTER);
            jsonObject.addProperty("token", PreferencesUtil.getToken());
            jsonObject.addProperty("numberOfPlayers", numberOfPlayers);
            jsonObject.addProperty("privateGame", privateGame);
            jsonObject.addProperty("gameType", gameType.ordinal());
            jsonObject.addProperty("enableBots", enableBots);
            webSocket.send(jsonObject.toString());
        }

        @Override
        public void onMessage(final String message) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onMessageGdx(message);
                }
            });
        }

        private void onMessageGdx(String message) {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            if (!json.has("command"))
                return;
            int command = json.get("command").getAsInt();
            switch (command) {
                case Constants.COMMAND_REGISTER:
                    if (!json.has("status"))
                        return;
                    if (json.get("status").getAsBoolean())
                        sendConnected();
                    else
                        sendDisconnected();
                    break;
                case Constants.COMMAND_WHOAMI:
                    if (!json.has("whoami"))
                        return;
                    sendWhoami(json.get("whoami").getAsInt());
                    break;
                case Constants.COMMAND_PLAYER_UPDATE:
                    if (!json.has("players"))
                        return;
                    ArrayList<Player> players = gson.fromJson(json.get("players").getAsString(),
                            new TypeToken<ArrayList<Player>>(){}.getType());
                    sendPlayerUpdate(players);
                    break;
                case Constants.COMMAND_DICE_UPDATE:
                    if (!json.has("dice"))
                        return;
                    ArrayList<Die> dice = gson.fromJson(json.get("dice").getAsString(),
                            new TypeToken<ArrayList<Die>>(){}.getType());
                    sendDieUpdate(dice);
                    break;
                case Constants.COMMAND_TURN_END:
                    if (!json.has("player") || !json.has("points"))
                        return;
                    sendTurnEnd(json.get("player").getAsInt(), json.get("points").getAsInt());
                    break;
                case Constants.COMMAND_ACTION_FAILED:
                    if (!json.has("action") || !json.has("failReason") ||
                            !json.has("player"))
                        return;
                    int action = json.get("action").getAsInt();
                    int failReason = json.get("failReason").getAsInt();
                    if (ControlEventListener.FailReason.values().length <= failReason)
                        return;
                    if (ControlEventListener.Action.values().length <= action)
                        return;
                    sendFailUpdate(ControlEventListener.Action.values()[action],
                            ControlEventListener.FailReason.values()[failReason], json.get("player").getAsInt());
                    break;
                case Constants.COMMAND_ACTIVE_POINTS:
                    if (!json.has("points"))
                        return;
                    sendActivePoints(json.get("points").getAsInt());
                    break;
                case Constants.COMMAND_GAME_END:
                    if (!json.has("players"))
                        return;
                    ArrayList<Player> orderedPlayers = gson.fromJson(json.get("players").getAsString(),
                            new TypeToken<ArrayList<Player>>(){}.getType());
                    sendWinner(orderedPlayers);
                    break;
                case Constants.COMMAND_LAST_ROUND:
                    if (!json.has("lastRoundStarter"))
                        return;
                    sendLastRound(json.get("lastRoundStarter").getAsInt());
                    break;
                case Constants.COMMAND_ZILCH_WARNING:
                    if (!json.has("player"))
                        return;
                    sendZilchWarning(json.get("player").getAsInt());
                    break;
                case Constants.COMMAND_ACHIEVEMENT:
                    if (!json.has("achievement") || !json.has("player"))
                        return;
                    int achievement = json.get("achievement").getAsInt();
                    int player = json.get("player").getAsInt();
                    if (AchievementHandler.Achievement.values().length <= achievement)
                        return;
                    sendAchievement(AchievementHandler.Achievement.values()[achievement], player);
                    break;
                case Constants.COMMAND_COUNTDOWN:
                    if (!json.has("milliseconds"))
                        return;
                    sendCountdown(json.get("milliseconds").getAsLong());
                    break;
                case Constants.COMMAND_ROLL_SUCCESS:
                    if (!json.has("player"))
                        return;
                    sendRollSuccess(json.get("player").getAsInt());
                    break;
                default:
                    Logger.debug("Unhandled web socket command %d", command);
                    break;
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.debug("WebClient closed. Reason: code %d - %s", code, reason);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    sendDisconnected();
                }
            });
        }

        @Override
        public void onError(Exception ex) {
            Logger.exception(ex);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    sendDisconnected();
                }
            });
        }
    }
}
