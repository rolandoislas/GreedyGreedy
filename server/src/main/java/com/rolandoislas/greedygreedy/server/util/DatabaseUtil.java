package com.rolandoislas.greedygreedy.server.util;

import com.badlogic.gdx.pay.Transaction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.data.GreedyPurchaseManagerConfig;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.data.Purchase;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.server.GreedySparkServerApi;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseUtil {
    private static Sql2o sql2o;
    private static int connections;
    private static int MAX_CONNECTIONS = 5;

    /**
     * Get new sql instance with a parsed connection url
     * @param serverUrl connection url
     * @return new instance
     */
    private static Sql2o getSql2oInstance(String serverUrl) {
        Pattern mysqlPattern = Pattern.compile("(.*://)(.*):(.*)@(.*)"); // (scheme)(user):(pass)@(url)
        Matcher mysqlMatches = mysqlPattern.matcher(serverUrl);
        if (!mysqlMatches.find()) {
            Logger.warn("Could not parse mysql database connection string.");
            System.exit(1);
        }
        String mysqlUrl = mysqlMatches.group(1) + mysqlMatches.group(4) + "?useSSL=false";
        String mysqlUsername = mysqlMatches.group(2);
        String mysqlPassword = mysqlMatches.group(3);
        return new Sql2o(mysqlUrl, mysqlUsername, mysqlPassword);
    }

    public static void setServer(String serverUrl) {
        sql2o = getSql2oInstance(serverUrl);
    }

    /**
     * Get the points associated with an oauthid
     * @param oauthid points for this id
     * @return points associated with the id
     * @throws GreedyException database error or invalid returned data
     */
    public static int getPoints(String oauthid) throws GreedyException {
        return getFieldFromPlayersTable(oauthid, "points", Integer.class);
    }

    /**
     * Check for existing user and create a new entry in mysql database if not found
     * @param oauthId id of user to create
     * @throws GreedyException database error
     */
    public static void createNewUser(String oauthId) throws GreedyException {
        String insertSql = "insert into players (oauth_id) select (:oauthid) from dual where not exists " +
                "(select 1 from players where oauth_id = :oauthid);";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(insertSql, false)
                    .addParameter("oauthid", oauthId)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
        // Add icon
        if (!userHasPurchase(oauthId, Icon.DIE_FIVE.ordinal(), Purchase.Type.ICON))
            addNewPurchase(oauthId, Icon.DIE_FIVE.ordinal(), Purchase.Type.ICON.ordinal());
    }

    static void addPointsToUser(String oauthid, int points) throws GreedyException {
        // Add the point value
        String updateSql = "update players set points = points + :addPoints where oauth_id = :oauthid";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(updateSql, false)
                    .addParameter("oauthid", oauthid)
                    .addParameter("addPoints", points)
                    .executeUpdate();
            releaseConnection(connection);
        } catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    private static void releaseConnection(Connection connection) {
        if (connection != null)
            connection.close();
        if (connections > 0)
            connections--;
    }

    private static Connection getConnection() {
        while (connections >= MAX_CONNECTIONS)
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {}
        Connection connection = sql2o.open();
        connections++;
        return connection;
    }

    public static int getIcon(String oauthid) throws GreedyException {
        return getFieldFromPlayersTable(oauthid, "icon", Integer.class);
    }

    private static <T> T getFieldFromPlayersTable(String oauthid, String field, Class<T> type) throws GreedyException {
        String query = "select " + field + " from players where oauth_id = :oauthid";
        Connection connection = null;
        try {
            connection = getConnection();
            List<T> icons = connection.createQuery(query)
                    .addParameter("oauthid", oauthid)
                    .executeAndFetch(type);
            releaseConnection(connection);
            if (icons.size() == 0)
                throw new GreedyException("No entry found for oauthid.");
            if (icons.size() > 1)
                throw new GreedyException("Multiple oauthids found in players table.");
            return icons.get(0);
        } catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    public static void setIcon(String oauthid, int icon) throws GreedyException {
        if (!userHasPurchase(oauthid, icon, Purchase.Type.ICON))
            throw new GreedyException("Icon not purchased");
        // Set icon
        String sql = "update players set icon = :icon where oauth_id = :oauthid";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(sql, false)
                    .addParameter("oauthid", oauthid)
                    .addParameter("icon", icon)
                    .executeUpdate();
            releaseConnection(connection);
        } catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    private static boolean userHasPurchase(String oauthid, int purchaseId, Purchase.Type purchaseType)
            throws GreedyException {
        List<Purchase> purchases = getPurchases(oauthid);
        for (Purchase purchase : purchases)
            if (purchase.getPurchaseType() == purchaseType.ordinal() && purchase.getItemId() == purchaseId)
                return true;
        return false;
    }

    public static List<Purchase> getPurchases(String oauthid) throws GreedyException {
        String query = "select item_id, purchase_type from purchases where oauth_id = :oauthid";
        Connection connection = null;
        try {
            connection = getConnection();
            List<Purchase> purchases = connection.createQuery(query)
                    .addParameter("oauthid", oauthid)
                    .executeAndFetch(Purchase.class);
            releaseConnection(connection);
            return purchases;
        } catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    /**
     * Check the player has enough points to purchase and deduct points before adding purchase
     * @param oauthid player id
     * @param id id of item to purchase
     * @param type type of item to purchase
     * @throws GreedyException database error
     */
    public static void purchase(String oauthid, int id, int type) throws GreedyException {
        if (userHasPurchase(oauthid, id, Purchase.Type.values()[type]))
            return;
        int points = getPoints(oauthid);
        JsonArray items = GreedySparkServerApi.getAvailableItems();
        for (JsonElement itemsElement : items) {
            JsonObject item = itemsElement.getAsJsonObject();
            if (item.get("id").getAsInt() == id && item.get("type").getAsInt() == type) {
                if (points < item.get("price").getAsInt())
                    throw new GreedyException("Not enough points to purchase");
                // Purchase
                addPointsToUser(oauthid, -item.get("price").getAsInt());
                try {
                    addNewPurchase(oauthid, id, type);
                }
                catch (GreedyException e) {
                    addPointsToUser(oauthid, item.get("price").getAsInt());
                    throw e;
                }
                return;
            }
        }
        throw new GreedyException("No item found for purchase");
    }

    /**
     * Add a purchase without any checks for points
     * @param oauthid player's id
     * @param id id of item
     * @param type item type
     * @throws GreedyException database error
     */
    private static void addNewPurchase(String oauthid, int id, int type) throws GreedyException {
        String insertSql = "insert into purchases (oauth_id, item_id, purchase_type) " +
                "values (:oauthid, :itemid, :purchasetype);";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(insertSql, false)
                    .addParameter("oauthid", oauthid)
                    .addParameter("itemid", id)
                    .addParameter("purchasetype", type)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    public static void purchaseIab(String oauthid, Transaction transaction) throws GreedyException {
        if (!purchaseIabExists(transaction.getOrderId())) {
            switch (transaction.getIdentifier()) {
                case GreedyPurchaseManagerConfig.PURCHASE_POINTS_200:
                    addPointsToUser(oauthid, 200);
                    addNewPurchaseIab(oauthid, transaction.getIdentifier(), transaction.getOrderId());
                    break;
                case GreedyPurchaseManagerConfig.PURCHASE_POINTS_600:
                    addPointsToUser(oauthid, 600);
                    addNewPurchaseIab(oauthid, transaction.getIdentifier(), transaction.getOrderId());
                    break;
                case GreedyPurchaseManagerConfig.PURCHASE_POINTS_1000:
                    addPointsToUser(oauthid, 1000);
                    addNewPurchaseIab(oauthid, transaction.getIdentifier(), transaction.getOrderId());
                    break;
            }
        }
    }

    private static void addNewPurchaseIab(String oauthid, String identifier, String orderId) throws GreedyException {
        String insertSql = "insert into purchases_iab (oauth_id, iab_item_id, iab_transaction_id) " +
                "values (:oauthid, :itemid, :transid);";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(insertSql, false)
                    .addParameter("oauthid", oauthid)
                    .addParameter("itemid", identifier)
                    .addParameter("transid", orderId)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }

    private static boolean purchaseIabExists(String orderId) throws GreedyException {
        String query = "select purchase_id from purchases_iab where iab_transaction_id = :orderId";
        Connection connection = null;
        try {
            connection = getConnection();
            List<Integer> purchases = connection.createQuery(query)
                    .addParameter("orderId", orderId)
                    .executeAndFetch(Integer.class);
            releaseConnection(connection);
            if (purchases.size() > 1)
                throw new GreedyException("Duplicate iab purchase registered");
            return purchases.size() == 1;
        } catch (Sql2oException e) {
            Logger.exception(e);
            releaseConnection(connection);
            throw new GreedyException(e);
        }
    }
}
