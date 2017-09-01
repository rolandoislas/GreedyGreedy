package com.rolandoislas.greedygreedy.server;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.server.util.AuthUtil;
import org.json.JSONObject;
import spark.*;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GreedySparkServer {
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

    static String handleAuthTokenVerify(Request request, Response response) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("valid", AuthUtil.verify(request));
        return jsonObject.toString();
    }

    private static void badRequest(String message) {
        throw halt(400, message);
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

    static String handleUserInfoRequest(Request request, Response response) {
        if (!AuthUtil.verify(request))
            unauthorized();
        return getUserInfo(AuthUtil.extractToken(request));
    }

    public static String getUserInfo(String token) {
        Webb webb = Webb.create();
        com.goebl.david.Response<JSONObject> webbResponse;
        try {
            webbResponse = webb.get(Constants.AUTH0_DOMAIN + "userinfo")
                    .header("Authorization", "Bearer " + token)
                    .ensureSuccess().asJsonObject();
        } catch (WebbException e) {
            throw halt(e.getResponse().getStatusCode(), e.getResponse().getStatusLine());
        }
        return webbResponse.getBody().toString();
    }
}
