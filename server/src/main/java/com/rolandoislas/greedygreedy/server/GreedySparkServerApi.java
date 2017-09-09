package com.rolandoislas.greedygreedy.server;

import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.server.PurchaseVerifierManager;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.data.Icon;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.data.Purchase;
import com.rolandoislas.greedygreedy.server.util.AuthUtil;
import com.rolandoislas.greedygreedy.server.util.DatabaseUtil;
import com.rolandoislas.greedygreedy.server.util.GreedyPurchaseVerifierAndroidGoogle;
import org.json.JSONObject;
import spark.*;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

public class GreedySparkServerApi {
    private static PurchaseVerifierManager purchaseVerifierManager;

    static {
        purchaseVerifierManager = new PurchaseVerifierManager();
        purchaseVerifierManager.addVerifier(new GreedyPurchaseVerifierAndroidGoogle());
    }

    private static HaltException halt(int code, String message) {
        JsonObject jsonObject = new JsonObject();
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        jsonObject.add("error", error);
        throw Spark.halt(code, jsonObject.toString());
    }

    private static void unauthorized() {
        throw halt(401, "Unauthorized");
    }

    static String handlePointsRequest(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        int points;
        try {
            points = DatabaseUtil.getPoints(oauthid);
        } catch (GreedyException e) {
            Logger.exception(e);
            throw halt(500, "Database error");
        }
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("points", points);
        return jsonResponse.toString();
    }

    static String handleVersionRequest(Request request, Response response) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("version", Constants.API_VERSION);
        return jsonResponse.toString();
    }

    static String handleUserInfoRequest(Request request, Response response) {
        if (!AuthUtil.verify(request))
            unauthorized();
        try {
            return getUserInfo(AuthUtil.extractToken(request));
        } catch (GreedyException e) {
            Logger.exception(e);
            throw halt(502, "Authentication server error.");
        }
    }

    static String handleAuthTokenVerify(Request request, Response response) {
        JsonObject jsonObject = new JsonObject();
        boolean valid = AuthUtil.verify(request);
        jsonObject.addProperty("valid", valid);
        // Check and create a new user in database if necessary
        if (!valid)
            unauthorized();
        try {
            DatabaseUtil.createNewUser(AuthUtil.getOauthId(AuthUtil.extractToken(request)));
        }
        catch (GreedyException e) {
            Logger.exception(e);
            throw halt(500, "Database connection error");
        }
        return jsonObject.toString();
    }

    static String handleAuthCallback(Request request, Response response) {
        response.type("text/html");
        String code = "";
        String type = "";
        Set<Map.Entry<String, String[]>> params = request.queryMap().toMap().entrySet();
        for (Map.Entry<String, String[]> param : params) {
            if (param.getKey().equals("code"))
                code = param.getValue()[0];
            if (param.getKey().equals("type"))
                type = param.getValue()[0];
            if (param.getKey().equals("error_description"))
                throw Spark.halt(400, param.getValue()[0]);
        }
        if (code.isEmpty() || type.isEmpty())
            throw Spark.halt(400, "400 Bad Request");
        Map<String, Object> model = new HashMap<>();
        model.put("authCode", code);
        model.put("port", Constants.LOGIN_CALLBACK_PORT);
        model.put("type", type);
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "callback.hbs"));
    }

    /**
     * Request user info from Auth0
     * @param token user access token
     * @return user info json
     */
    public static String getUserInfo(String token) throws GreedyException {
        Webb webb = Webb.create();
        com.goebl.david.Response<JSONObject> webbResponse;
        try {
            webbResponse = webb.get(Constants.AUTH0_DOMAIN + "userinfo")
                    .header("Authorization", "Bearer " + token)
                    .ensureSuccess().asJsonObject();
        } catch (WebbException e) {
            throw new GreedyException(e);
        }
        return webbResponse.getBody().toString();
    }

    static String handleIconRequest(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        int icon;
        try {
            icon = DatabaseUtil.getIcon(oauthid);
        } catch (GreedyException e) {
            Logger.exception(e);
            throw halt(500, "Database error");
        }
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("icon", icon);
        return jsonResponse.toString();
    }

    static String handleIconPutRequest(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        JsonObject payload = new Gson().fromJson(request.body(), JsonObject.class);
        if (payload == null || !payload.has("icon"))
            throw halt(400, "Bad request");
        try {
            DatabaseUtil.setIcon(oauthid, payload.get("icon").getAsInt());
        } catch (GreedyException e) {
            Logger.exception(e);
            if (e.getMessage().equalsIgnoreCase("Icon not purchased"))
                throw halt(402, "Icon not purchased");
            throw halt(500, "Database error");
        }
        return "";
    }

    /**
     * Returns the purchases made by the authenticated user
     * @param request spark request
     * @param response spark response
     * @return json array
     */
    static String handlePurchasesRequest(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        List<Purchase> purchases;
        try {
            purchases = DatabaseUtil.getPurchases(oauthid);
        } catch (GreedyException e) {
            Logger.exception(e);
            throw halt(500, "Database error");
        }
        return new Gson().toJson(purchases);
    }

    /**
     * Return all available items for purchase
     * @param request spark request
     * @param response spark response
     * @return json array
     */
    static String handlePurchasesAllRequest(Request request, Response response) {
        return getAvailableItems().toString();
    }

    public static JsonArray getAvailableItems() {
        JsonArray items = new JsonArray();
        for (Icon icon : Icon.values()) {
            JsonObject jsonIcon = new JsonObject();
            jsonIcon.addProperty("id", icon.ordinal());
            jsonIcon.addProperty("type", Purchase.Type.ICON.ordinal());
            jsonIcon.addProperty("price", 200);
            items.add(jsonIcon);
        }
        return items;
    }

    static String handlePurchaseViaPoints(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        JsonObject req = new Gson().fromJson(request.body(), JsonObject.class);
        if (req == null || !req.has("id") || !req.has("type"))
            throw halt(400, "Bad request");
        if (req.get("type").getAsInt() == Purchase.Type.ICON.ordinal()) {
            if (req.get("id").getAsInt() >= Icon.values().length || req.get("id").getAsInt() < 0)
                throw halt(400, "Bad request");
        }
        else
            throw halt(400, "Bad request");
        try {
            DatabaseUtil.purchase(oauthid, req.get("id").getAsInt(), req.get("type").getAsInt());
        } catch (GreedyException e) {
            Logger.exception(e);
            if (e.getMessage().equalsIgnoreCase("Not enough points to purchase"))
                throw halt(402, "Not enough points to purchase");
            throw halt(500, "Database error");
        }
        return "OK";
    }

    static String handlePurchaseViaIab(Request request, Response response) {
        String token = AuthUtil.extractToken(request);
        if (!AuthUtil.verify(token))
            unauthorized();
        String oauthid = AuthUtil.getOauthId(token);
        Transaction transaction = new Gson().fromJson(request.body(), Transaction.class);
        boolean purchased = purchaseVerifierManager.isValid(transaction);
        if (purchased) {
            try {
                DatabaseUtil.purchaseIab(oauthid, transaction);
            } catch (GreedyException e) {
                throw halt(500, "Database error");
            }
        }
        else
            throw halt(400, "Bad request");
        return "OK";
    }
}
