package com.rolandoislas.greedygreedy.server.util;

import com.rolandoislas.greedygreedy.core.util.GameController;
import com.rolandoislas.greedygreedy.server.data.Client;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;

public class ClientHandler {
    private ArrayList<Client> clients = new ArrayList<>();

    public void removeClient(Session socket) {
        synchronized (this) {
        Client client = getClient(socket);
        if (client != null)
            clients.remove(client);
        }
    }

    public void addClient(Session socket) {
        synchronized (this) {
            clients.add(new Client(socket));
        }
    }

    public boolean authenticateClient(Session socket, String token, int numberOfPlayers, boolean privateGame,
                                      int gameType, boolean enableBots) {
        synchronized (this) {
            if (AuthUtil.verify(token)) {
                Client client = getClient(socket);
                if (client == null)
                    return false;
                client.setToken(token);
                client.setGameSize(numberOfPlayers);
                client.setPrivateGame(privateGame);
                if (gameType >= GameController.GameType.values().length)
                    return false;
                client.setGameType(GameController.GameType.values()[gameType]);
                client.setEnableBots(enableBots);
                client.setSearching(true);
                return true;
            }
        }
        return false;
    }

    public Client getClient(Session socket) {
        synchronized (this) {
            for (Client client : clients)
                if (client.getSocket().equals(socket))
                    return client;
        }
        return null;
    }

    public ArrayList<Client> getSearchingPlayers() {
        synchronized (this) {
            ArrayList<Client> searchingClients = new ArrayList<>();
            for (Client client : clients)
                if (client.getToken() != null && !client.getToken().isEmpty() && client.isSearching())
                    searchingClients.add(client);
            return searchingClients;
        }
    }

    public Client getClient(String oauthid) {
        synchronized (this) {
            for (Client client : clients) {
                if (client.getToken() == null || client.getToken().isEmpty())
                    continue;
                String idFromToken = AuthUtil.getOauthId(client.getToken());
                if (idFromToken != null && idFromToken.equals(oauthid))
                    return client;
            }
        }
        return null;
    }
}
