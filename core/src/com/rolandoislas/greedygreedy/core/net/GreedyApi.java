package com.rolandoislas.greedygreedy.core.net;

import com.badlogic.gdx.pay.Transaction;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GreedyApi {

    public static boolean confirmToken() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get(getApiUrl() + "api/auth/token/verify")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            if (e.getResponse().getStatusCode() == 401)
                return false;
            throw new GreedyException(e);
        }
        try {
            return response.getBody().getBoolean("valid");
        } catch (JSONException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }

    public static String getApiUrl() {
        if (!Constants.FORCE_API_URL.isEmpty()) {
            Logger.warn("Using forced API URL: %s", Constants.FORCE_API_URL);
            return Constants.FORCE_API_URL;
        }
        else if (GreedyClient.args.localCallback)
            return Constants.AUTH0_AUDIENCE_LOCAL;
        else if (GreedyClient.args.devCallback || Constants.FORCE_DEV_API) {
            Logger.warn("Using dev API URL: %s", Constants.AUTH0_AUDIENCE_DEV);
            return Constants.AUTH0_AUDIENCE_DEV;
        }
        return Constants.AUTH0_AUDIENCE;
    }

    private static String getAccessToken() throws GreedyException {
        String accessToken = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL)
                .getString(Constants.PREF_ACCESS_TOKEN);
        if (accessToken == null || accessToken.isEmpty())
            throw new GreedyException("No access token cached.");
        return accessToken;
    }

    public static String getUserInfo() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get(getApiUrl() + "api/auth/userinfo")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        return response.getBody().toString();
    }

    private static String getAuthHeader() throws GreedyException {
        return "Bearer " + getAccessToken();
    }

    public static int getPoints() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get(getApiUrl() + "api/info/points")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        try {
            return response.getBody().getInt("points");
        }
        catch (JSONException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }

    public static int getVersion() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get(getApiUrl() + "api/info/version")
                    .ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        try {
            return response.getBody().getInt("version");
        }
        catch (JSONException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }

    public static Icon getIcon() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get(getApiUrl() + "api/info/icon")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        try {
            int iconOrdinal = response.getBody().getInt("icon");
            return Icon.values()[iconOrdinal];
        }
        catch (JSONException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        catch (IndexOutOfBoundsException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }

    public static String getPurchases() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONArray> response;
        try {
            response = webb.get(getApiUrl() + "api/info/purchases")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonArray();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        return response.getBody().toString();
    }

    public static String getAllAvailablePurchases() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONArray> response;
        try {
            response = webb.get(getApiUrl() + "api/info/purchases/all")
                    .ensureSuccess().asJsonArray();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        return response.getBody().toString();
    }

    public static void putIcon(int id) throws GreedyException {
        JsonObject put = new JsonObject();
        put.addProperty("icon", id);
        Webb webb = Webb.create();
        try {
            webb.put(getApiUrl() + "api/info/icon")
                    .header("Authentication", getAuthHeader()).ensureSuccess()
                    .body(put.toString()).asVoid();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }

    public static void purchase(int id, int type) throws GreedyException {
        JsonObject post = new JsonObject();
        post.addProperty("id", id);
        post.addProperty("type", type);
        Webb webb = Webb.create();
        try {
            webb.post(getApiUrl() + "api/purchase/points")
                    .header("Authentication", getAuthHeader()).ensureSuccess()
                    .body(post.toString()).asVoid();
        }
        catch (WebbException e) {
            Logger.exception(e);
            if (e.getResponse() != null && e.getResponse().getStatusCode() == 402) // Payment Required
                throw new GreedyException("Not enough points to purchase");
            throw new GreedyException(e);
        }
    }

    public static void purchaseIab(Transaction transaction) throws GreedyException {
        Webb webb = Webb.create();
        try {
            webb.post(getApiUrl() + "api/purchase/iab")
                    .header("Authentication", getAuthHeader()).ensureSuccess()
                    .body(new Gson().toJson(transaction)).asVoid();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
    }
}
