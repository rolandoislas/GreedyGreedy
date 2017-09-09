package com.rolandoislas.greedygreedy.server.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.IDie;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Player;
import com.rolandoislas.greedygreedy.core.event.ControlEventListener;
import com.rolandoislas.greedygreedy.core.util.*;
import com.rolandoislas.greedygreedy.server.data.Client;
import org.eclipse.jetty.websocket.api.Session;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class GameHandler implements ControlEventListener {
    private final Jedis redis;
    private ClientHandler clientHandler;
    private GameController gamecontroller;
    private Gson gson;
    private long lastGameUpdateTick;
    private ReentrantLock redisMutex;
    private String activeGame;

    public GameHandler(String redisServer, String mysqlServer) {
        clientHandler = new ClientHandler();
        redis = new Jedis(redisServer);
        gson = new Gson();
        gamecontroller = new AiController(0, GameController.GameType.ANY, false, false);
        gamecontroller.addListener(this);
        redisMutex = new ReentrantLock();
        activeGame = null;
    }

    public void run() {
        if (System.currentTimeMillis() - lastGameUpdateTick < 100)
            return;
        lastGameUpdateTick = System.currentTimeMillis();
        updateSearchingPlayers();
        updateGames();
    }

    private void updateGames() {
        redisMutex.lock();
        if (redis.hlen("games") == 0) {
            redisMutex.unlock();
            return;
        }
        Map<String, String> gamesBulk = redis.hgetAll("games");
        // Structure games into a more parsable list
        HashMap<String, HashMap<String, String>> games = new HashMap<>();
        for (Map.Entry<String, String> entry : gamesBulk.entrySet()) {
            String[] gameid = entry.getKey().split(":");
            if (gameid.length != 2)
                continue;
            HashMap<String, String> game = games.getOrDefault(gameid[0], new HashMap<>());
            game.put(gameid[1], entry.getValue());
            games.put(gameid[0], game);
        }
        // Perform game ticks
        for (HashMap.Entry<String, HashMap<String, String>> game : games.entrySet()) {
            String gameid = game.getKey();
            JsonArray players = gson.fromJson(game.getValue().get("players"), JsonArray.class);
            String gamestate = game.getValue().get("state");
            if (players == null || gamestate == null || gamestate.isEmpty())
                continue;
            // Perform actions for bots
            gamecontroller.stop();
            activeGame = gameid;
            try {
                gamecontroller.loadState(gamestate);
            } catch (GreedyException e) { // Catch API version mismatch
                Logger.exception(e);
                destroyGame(gameid);
                continue;
            }
            gamecontroller.start();
            // Check player time
            for (JsonElement playerElement : players) {
                JsonObject player = playerElement.getAsJsonObject();
                if (!player.get("active").getAsBoolean())
                    continue;
                long playerTime = player.get("time").getAsLong();
                boolean isConnected = player.get("connected").getAsBoolean();
                if (System.currentTimeMillis() - playerTime > Constants.TURN_TIME || !isConnected)
                    ((AiController) gamecontroller).forceNextPlayerActive();
                break;
            }
            // Player state (Redis) is invalid after the gamestate has changed vis forceNextPlayerActive() or act().
            // Fetch a new one if more operations are needed in future.
            gamecontroller.act(1);
            gamecontroller.stop();
            activeGame = null;
            // Save the state if it has changed
            String newState = gamecontroller.saveState();

            /* === DEV WIN === */ // Comment this out!
            /*Logger.warn("DEV WIN enabled!");
            JsonObject stateJson = gson.fromJson(newState, JsonObject.class);
            ArrayList<Player> playersArray = gson.fromJson(stateJson.get("players").getAsString(),
                    new TypeToken<ArrayList<Player>>(){}.getType());
            playersArray.get(0).setScore(AiController.SCORE_WIN - 5000);
            playersArray.get(1).setScore(AiController.SCORE_WIN);
            playersArray.get(2).setScore(AiController.SCORE_WIN);
            playersArray.get(3).setScore(AiController.SCORE_WIN);
            stateJson.addProperty("players", gson.toJson(playersArray));
            newState = stateJson.toString();*/
            /* === DEV WIN === */

            if (!newState.equals(gamestate)) {
                boolean gameOver = gson.fromJson(newState, JsonObject.class).get("gameOver").getAsBoolean();
                if (!gameOver)
                    redis.hset("games", gameid + ":state", newState);
            }
        }
        redisMutex.unlock();
    }

    private void updateSearchingPlayers() {
        ArrayList<Client> searchingPlayers = clientHandler.getSearchingPlayers();
        if (searchingPlayers.size() < 1)
            return;
        redisMutex.lock();
        ArrayList<Client> matches = new ArrayList<>();
        // Loop through every player
        for (Client searchingPlayer : searchingPlayers) {
            // Reconnect a player
            String id = AuthUtil.getOauthId(searchingPlayer.getToken());
            if (redis.hexists("players", id)) {
                reconnectPlayer(searchingPlayer);
                redisMutex.unlock();
                return;
            }
            // Search parameters
            int gameSize = searchingPlayer.getGameSize();
            boolean privateGame = searchingPlayer.isPrivateGame();
            String privateGameId = searchingPlayer.getPrivateGameId();
            GameController.GameType gameType = searchingPlayer.getGameType();
            boolean enableBots = searchingPlayer.areBotsEnabled();
            String oauthId = AuthUtil.getOauthId(searchingPlayer.getToken());
            // Start a solo "muliplayer" game
            if (gameSize == 1) {
                createGame(searchingPlayer);
                redisMutex.unlock();
                return;
            }
            // Search for compatible players
            matches.clear();
            for (Client potentialMatch : searchingPlayers) {
                String potentialMatchOauthId = AuthUtil.getOauthId(potentialMatch.getToken());
                if (potentialMatchOauthId == null)
                    continue;
                if ((!potentialMatchOauthId.equals(oauthId)) &&
                        potentialMatch.getGameSize() == gameSize &&
                        (potentialMatch.getGameType().equals(GameController.GameType.ANY) ||
                                gameType.equals(potentialMatch.getGameType())) &&
                        potentialMatch.areBotsEnabled() == enableBots &&
                        (!privateGame || potentialMatch.getPrivateGameId().equals(privateGameId))) {
                    matches.add(potentialMatch);
                    // Enough players found to start
                    if (matches.size() + 1 == gameSize) {
                        matches.add(searchingPlayer);
                        createGame(matches.toArray(new Client[matches.size()]));
                        redisMutex.unlock();
                        return;
                    }
                }
            }
        }
        redisMutex.unlock();
    }

    private void reconnectPlayer(Client player) {
        sendWhoAmI(player);
        sendPlayers(player);
        sendDice(player);
        sendActivePoints(player);
        sendCountdown(player);
        player.setSearching(false);
    }

    private void sendCountdown(Client player) {
        redisMutex.lock();
        String id = AuthUtil.getOauthId(player.getToken());
        String gameid = redis.hget("players", id);
        JsonArray players = gson.fromJson(redis.hget("games", gameid + ":players"), JsonArray.class);
        long timeRemaining = 0;
        for (JsonElement playerElement : players) {
            JsonObject playerObject = playerElement.getAsJsonObject();
            if (playerObject.get("active").getAsBoolean()) {
                timeRemaining = Constants.TURN_TIME -
                        (System.currentTimeMillis() - playerObject.get("time").getAsLong());
                break;
            }
        }
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_COUNTDOWN);
        command.addProperty("milliseconds", timeRemaining);
        player.send(command.toString());
        redisMutex.unlock();
    }

    private void sendActivePoints(Client player) {
        redisMutex.lock();
        String id = AuthUtil.getOauthId(player.getToken());
        String gameid = redis.hget("players", id);
        JsonObject gameState = gson.fromJson(redis.hget("games", gameid + ":state"), JsonObject.class);
        int activePoints = gameState.get("activePoints").getAsInt();
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ACTIVE_POINTS);
        command.addProperty("points", activePoints);
        player.send(command.toString());
        redisMutex.unlock();
    }

    private void sendDice(Client player) {
        redisMutex.lock();
        String id = AuthUtil.getOauthId(player.getToken());
        String gameid = redis.hget("players", id);
        JsonObject diceCommand = new JsonObject();
        diceCommand.addProperty("command", Constants.COMMAND_DICE_UPDATE);
        JsonObject gameState = gson.fromJson(redis.hget("games", gameid + ":state"), JsonObject.class);
        diceCommand.addProperty("dice", gameState.get("dice").getAsString());
        player.send(diceCommand.toString());
        redisMutex.unlock();
    }

    private void sendPlayers(Client player) {
        redisMutex.lock();
        String id = AuthUtil.getOauthId(player.getToken());
        String gameid = redis.hget("players", id);
        JsonObject playersCommand = new JsonObject();
        playersCommand.addProperty("command", Constants.COMMAND_PLAYER_UPDATE);
        JsonObject gameState = gson.fromJson(redis.hget("games", gameid + ":state"), JsonObject.class);
        playersCommand.addProperty("players", gameState.get("players").getAsString());
        player.send(playersCommand.toString());
        redisMutex.unlock();
    }

    private void sendWhoAmI(Client player) {
        redisMutex.lock();
        String id = AuthUtil.getOauthId(player.getToken());
        String gameid = redis.hget("players", id);
        JsonArray players = gson.fromJson(redis.hget("games", gameid + ":players"), JsonArray.class);
        int whoami = 0;
        for (JsonElement pid : players) {
            if (pid.getAsJsonObject().get("id").getAsString().equals(id))
                break;
            whoami++;
        }
        JsonObject whoamiCommand = new JsonObject();
        whoamiCommand.addProperty("command", Constants.COMMAND_WHOAMI);
        whoamiCommand.addProperty("whoami", whoami);
        player.send(whoamiCommand.toString());
        redisMutex.unlock();
    }

    private void createGame(Client... players) {
        redisMutex.lock();
        String gameid;
        do {
            gameid = UUID.randomUUID().toString();
        } while (redis.hexists("games", gameid));
        // Populate players
        JsonArray playerIds = new JsonArray();
        for (Client player : players) {
            // No longer searching
            player.setSearching(false);
            // Generate player data
            String id = AuthUtil.getOauthId(player.getToken());
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("id", id);
            playerObject.addProperty("time", System.currentTimeMillis());
            playerObject.addProperty("active", players[0] == player);
            playerObject.addProperty("connected", true);
            playerIds.add(playerObject);
            // Add to player index
            redis.hsetnx("players", id, gameid);
        }
        redis.hsetnx("games", gameid + ":players", playerIds.toString());
        // Create a the new game
        GameController.GameType gameType = GameController.GameType.NORMAL;
        for (Client player : players) {
            if (!player.getGameType().equals(GameController.GameType.ANY)) {
                gameType = player.getGameType();
                break;
            }
        }
        gamecontroller = new AiController(players[0].getGameSize(), gameType,
                players[0].areBotsEnabled(), false);
        activeGame = gameid;
        gamecontroller.addListener(this);
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Icon> icons = new ArrayList<>();
        for (Client player : players) {
            names.add(AuthUtil.getName(player.getToken()));
            try {
                icons.add(Icon.values()[DatabaseUtil.getIcon(AuthUtil.getOauthId(player.getToken()))]);
            } catch (GreedyException e) {
                Logger.exception(e);
                icons.add(Icon.values()[0]);
            }
        }
        ((AiController)gamecontroller).setNames(names);
        ((AiController)gamecontroller).setIcons(icons);
        gamecontroller.start();
        gamecontroller.stop();
        activeGame = null;
        redis.hsetnx("games", gameid + ":state", gamecontroller.saveState());
        // Send update
        for (Client player : players)
            reconnectPlayer(player);
        redisMutex.unlock();
    }

    private void destroyGame(String gameId) {
        redisMutex.lock();
        JsonArray players = gson.fromJson(redis.hget("games", gameId + ":players"), JsonArray.class);
        ArrayList<String> oauthIds = new ArrayList<>();
        for (JsonElement playerElement : players)
            oauthIds.add(playerElement.getAsJsonObject().get("id").getAsString());
        redis.hdel("players", oauthIds.toArray(new String[oauthIds.size()]));
        redis.hdel("games", gameId + ":state", gameId + ":players");
        redisMutex.unlock();
    }

    public void addClient(Session socket) {
        clientHandler.addClient(socket);
    }

    public void removeClient(Session socket) {
        Client player = clientHandler.getClient(socket);
        clientHandler.removeClient(socket);
        // Set the player to disconnected in the game state
        if (player == null || player.getToken() == null || player.getToken().isEmpty())
            return;
        String oauthid = AuthUtil.getOauthId(player.getToken());
        redisMutex.lock();
        String gameid = redis.hget("players", oauthid);
        JsonArray players = gson.fromJson(redis.hget("games", gameid + ":players"), JsonArray.class);
        if (players == null) {
            redisMutex.unlock();
            return;
        }
        for (JsonElement playerElement : players) {
            if (playerElement.getAsJsonObject().get("id").getAsString().equals(oauthid)) {
                playerElement.getAsJsonObject().addProperty("connected", false);
                break;
            }
        }
        redis.hset("games", gameid + ":players", players.toString());
        // Check if there is any players still connected. If not, destroy game without rewarding points.
        boolean arePlayersConnected = false;
        for (JsonElement playerElement : players) {
            if (playerElement.getAsJsonObject().get("connected").getAsBoolean()) {
                arePlayersConnected = true;
                break;
            }
        }
        if (!arePlayersConnected)
            destroyGame(gameid);
        redisMutex.unlock();
    }

    public void handleCommand(Session socket, JsonObject json) {
        Client player = clientHandler.getClient(socket);
        boolean authenticated = player.getToken() != null && !player.getToken().isEmpty();
        int command = json.get("command").getAsInt();
        switch (command) {
            case Constants.COMMAND_REGISTER:
                handleRegister(socket, json);
                break;
            case Constants.COMMAND_CLICK_DIE:
                if (authenticated)
                    handleClickDie(socket, json);
                break;
            case Constants.COMMAND_CLICK_ROLL:
                if (authenticated)
                    handleClickRoll(socket, json);
                break;
            case Constants.COMMAND_CLICK_STOP:
                if (authenticated)
                    handleClickStop(socket, json);
                break;
            default:
                Logger.debug("Unhandled command %d", command);
                break;
        }
    }

    private void handleClickStop(Session socket, JsonObject json) {
        handleClickAction(socket, json, ClickAction.STOP);
    }

    private void handleClickRoll(Session socket, JsonObject json) {
        handleClickAction(socket, json, ClickAction.ROLL);
    }

    private void handleClickDie(Session socket, JsonObject json) {
        handleClickAction(socket, json, ClickAction.DIE);
    }

    private void handleClickAction(Session socket, JsonObject json, ClickAction clickAction) {
        Client player = clientHandler.getClient(socket);
        if (player == null)
            return;
        String oauthId = AuthUtil.getOauthId(player.getToken());
        redisMutex.lock();
        if (!redis.hexists("players", oauthId)) {
            redisMutex.unlock();
            return;
        }
        String gameid = redis.hget("players", oauthId);
        // Check if the player is active
        JsonArray players = gson.fromJson(redis.hget("games", gameid + ":players"), JsonArray.class);
        for (JsonElement playerElement : players) {
            String pid = playerElement.getAsJsonObject().get("id").getAsString();
            boolean active = playerElement.getAsJsonObject().get("active").getAsBoolean();
            if (pid.equals(oauthId)) {
                if (!active) {
                    redisMutex.unlock();
                    return;
                }
                break;
            }
        }
        // Forward action to controller
        String gamestate = redis.hget("games", gameid + ":state");
        gamecontroller.stop();
        activeGame = gameid;
        try {
            gamecontroller.loadState(gamestate);
        } catch (GreedyException e) {
            Logger.exception(e);
            destroyGame(gameid);
            redisMutex.unlock();
            return;
        }
        gamecontroller.start();
        switch (clickAction) {
            case DIE:
                if (!json.has("dieNum")) {
                    redisMutex.unlock();
                    return;
                }
                gamecontroller.clickDie(json.get("dieNum").getAsInt());
                break;
            case ROLL:
                gamecontroller.rollClicked();
                break;
            case STOP:
                gamecontroller.stopClicked();
                break;
            default:
                break;
        }
        gamecontroller.stop();
        activeGame = null;
        String newGamestate = gamecontroller.saveState();
        if (!gamestate.equals(newGamestate))
            redis.hset("games", gameid + ":state", newGamestate);
        redisMutex.unlock();
    }

    private void handleRegister(Session socket, JsonObject json) {
        if (!json.has("token") || !json.has("numberOfPlayers") ||
                !json.has("privateGame") || !json.has("gameType") ||
                !json.has("enableBots"))
            return;
        boolean authenticated = clientHandler.authenticateClient(socket, json.get("token").getAsString(),
                json.get("numberOfPlayers").getAsInt(), json.get("privateGame").getAsBoolean(),
                json.get("gameType").getAsInt(), json.get("enableBots").getAsBoolean());
        JsonObject response = new JsonObject();
        response.addProperty("command", Constants.COMMAND_REGISTER);
        response.addProperty("status", authenticated);
        clientHandler.getClient(socket).send(response.toString());
        if (authenticated) {
            // Set the player to connected in the game state
            Client player = clientHandler.getClient(socket);
            if (player == null || player.getToken() == null || player.getToken().isEmpty())
                return;
            String oauthid = AuthUtil.getOauthId(player.getToken());
            redisMutex.lock();
            String gameid = redis.hget("players", oauthid);
            if (gameid == null || gameid.isEmpty()) {
                redisMutex.unlock();
                return;
            }
            JsonArray players = gson.fromJson(redis.hget("games", gameid + ":players"), JsonArray.class);
            for (JsonElement playerElement : players) {
                if (playerElement.getAsJsonObject().get("id").getAsString().equals(oauthid)) {
                    playerElement.getAsJsonObject().addProperty("connected", true);
                    break;
                }
            }
            redis.hset("games", gameid + ":players", players.toString());
            redisMutex.unlock();
        }
    }

    private boolean canHandleGameControllerEvent() {
        assert redisMutex.isLocked();
        assert activeGame != null;
        return redis.hexists("games", activeGame + ":players");
    }

    /**
     * Assigns points to the top three players and updates the mysql database.
     * @param playersArray sorted players from gamecontroller
     * @param players redis players array
     */
    private void givePointsToPlayers(ArrayList<Player> playersArray, JsonArray players) {
        // Determine if points should be awarded
        Client client = null;
        for (JsonElement playerElement : players) {
            client = clientHandler.getClient(playerElement.getAsJsonObject().get("id").getAsString());
            if (client != null && !client.getGameType().equals(GameController.GameType.ANY))
                break;
        }
        if (client == null)
            return;
        GameOptionsUtil.PointValue gamePointsValue = GameOptionsUtil.parseOptions(client.getGameSize(),
                client.areBotsEnabled(), client.isPrivateGame(), client.getGameType());
        if (!gamePointsValue.equals(GameOptionsUtil.PointValue.FULL_POINTS))
            return;
        // Assign points to be distributed
        HashMap<String, Integer> pointDistributions = new HashMap<>();
        int realPlayerIndex = 0;
        for (Player player : playersArray) {
            if (!player.isBot()) {
                String oauthid = players.get(realPlayerIndex).getAsJsonObject().get("id").getAsString();
                pointDistributions.put(oauthid, Constants.WINNING_POINTS_DISTRIBUTIONS[playersArray.indexOf(player)]);
                realPlayerIndex++;
            }
        }
        // Add the point value
        for (HashMap.Entry<String, Integer> pointDistribution : pointDistributions.entrySet()) {
            try {
                DatabaseUtil.addPointsToUser(pointDistribution.getKey(), pointDistribution.getValue());
            } catch (GreedyException e) {
                Logger.exception(e);
            }
        }
    }

    /**
     * Sends a command to all players in a json array that match valid connected clients
     * @param players Redis hash set (games:players)
     * @param command command data
     */
    private void sendToAll(JsonArray players, JsonObject command) {
        for (JsonElement playerElement : players) {
            String playerId = playerElement.getAsJsonObject().get("id").getAsString();
            Client player = clientHandler.getClient(playerId);
            if (player == null)
                continue;
            player.send(command.toString());
        }
    }

    /*
    Game Controller Listener forwarding
     */

    @Override
    public void connected() {
        // ignore
    }

    @Override
    public void disConnected() {
        // ignore
    }

    @Override
    public void dieUpdate(ArrayList<IDie> dice) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process dieUpdate event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_DICE_UPDATE);
        command.addProperty("dice", gson.toJson(dice));
        sendToAll(players, command);
        redisMutex.unlock();
    }

    @Override
    public void playerUpdate(ArrayList<Player> playersArray) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process playerUpdate event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send playerUpdate command
        JsonObject playerUpdateCommand = new JsonObject();
        playerUpdateCommand.addProperty("command", Constants.COMMAND_PLAYER_UPDATE);
        playerUpdateCommand.addProperty("players", gson.toJson(playersArray));
        sendToAll(players, playerUpdateCommand);
        // Activate player
        int activePlayer = -1;
        ArrayList<Player> noBots = new ArrayList<>();
        for (Player player : playersArray)
            if (!player.isBot())
                noBots.add(player);
        for (Player player : noBots) {
            if (player.isActive()) {
                activePlayer = noBots.indexOf(player);
                break;
            }
        }
        if (activePlayer > -1) {
            players.get(activePlayer).getAsJsonObject().addProperty("active", true);
            if (players.get(activePlayer).getAsJsonObject().get("time").getAsLong() == -1)
                players.get(activePlayer).getAsJsonObject().addProperty("time", System.currentTimeMillis());
            redis.hset("games", activeGame + ":players", players.toString());
        }
        // Send countdown time command
        long timeRemaining = activePlayer > -1 ? Constants.TURN_TIME - (System.currentTimeMillis() -
                players.get(activePlayer).getAsJsonObject().get("time").getAsLong()) : 0;
        JsonObject countdownTimeCommand = new JsonObject();
        countdownTimeCommand.addProperty("command", Constants.COMMAND_COUNTDOWN);
        countdownTimeCommand.addProperty("milliseconds", timeRemaining);
        sendToAll(players, countdownTimeCommand);
        redisMutex.unlock();
    }

    @Override
    public void whoami(int playerNum) {
        // ignore
    }

    @Override
    public void actionFailed(Action action, FailReason failReason, int player) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process actionFailed event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ACTION_FAILED);
        command.addProperty("action", action.ordinal());
        command.addProperty("failReason", failReason.ordinal());
        command.addProperty("player", player);
        sendToAll(players, command);
        redisMutex.unlock();
    }

    @Override
    public void activePoints(int activePoints) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process activePoints event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ACTIVE_POINTS);
        command.addProperty("points", activePoints);
        sendToAll(players, command);
        redisMutex.unlock();
    }

    @Override
    public void turnEnd(int playerNum, int points) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process turnEnd event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_TURN_END);
        command.addProperty("player", playerNum);
        command.addProperty("points", points);
        sendToAll(players, command);
        // Deactivate players
        for (JsonElement player : players) {
            player.getAsJsonObject().addProperty("time", -1);
            player.getAsJsonObject().addProperty("active", false);
        }
        redis.hset("games", activeGame + ":players", players.toString());
        redisMutex.unlock();
    }

    @Override
    public void gameEnd(ArrayList<Player> playersArray) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process gameEnd event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_GAME_END);
        command.addProperty("players", gson.toJson(playersArray));
        sendToAll(players, command);
        // Handle game end
        givePointsToPlayers(playersArray, players);
        destroyGame(activeGame);
        redisMutex.unlock();
    }

    @Override
    public void lastRound(int lastRoundStarter) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process lastRound event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_LAST_ROUND);
        command.addProperty("lastRoundStarter", lastRoundStarter);
        sendToAll(players, command);
        redisMutex.unlock();
    }

    @Override
    public void zilchWarning(int playerNum) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process zilchWarning event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ZILCH_WARNING);
        command.addProperty("player", playerNum);
        sendToAll(players, command);
        redisMutex.unlock();
    }

    @Override
    public void achievement(AchievementHandler.Achievement achievement, int playerNum) {
        redisMutex.lock();
        if (!canHandleGameControllerEvent()) {
            redisMutex.unlock();
            Logger.debug("Failed to process achievement event.");
            return;
        }
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        // Send command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ACHIEVEMENT);
        command.addProperty("achievement", achievement.ordinal());
        command.addProperty("player", playerNum);
        sendToAll(players, command);
        // TODO unlock achievements
        redisMutex.unlock();
    }

    @Override
    public void countdown(long milliseconds) {
        // Ignore
    }

    @Override
    public void rollSuccess(int player) {
        redisMutex.lock();
        // Reset player turn time on a successful roll
        JsonArray players = gson.fromJson(redis.hget("games", activeGame + ":players"), JsonArray.class);
        for (JsonElement playerElement : players)
            if (playerElement.getAsJsonObject().get("active").getAsBoolean())
                playerElement.getAsJsonObject().addProperty("time", System.currentTimeMillis());
        redis.hset("games", activeGame + ":players", players.toString());
        // Forward rollSuccess command
        JsonObject command = new JsonObject();
        command.addProperty("command", Constants.COMMAND_ROLL_SUCCESS);
        command.addProperty("player", player);
        sendToAll(players, command);
        redisMutex.unlock();
    }

    private enum  ClickAction {
        ROLL, STOP, DIE
    }
}
