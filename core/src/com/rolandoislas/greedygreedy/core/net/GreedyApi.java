package com.rolandoislas.greedygreedy.core.net;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class GreedyApi {

    public static boolean confirmToken() throws GreedyException {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get((GreedyClient.args.localCallback ? Constants.AUTH0_AUDIENCE_LOCAL :
                    Constants.AUTH0_AUDIENCE) + "api/auth/token/verify")
                    .header("Authentication", getAuthHeader()).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
        try {
            return response.getBody().getBoolean("valid");
        } catch (JSONException e) {
            Logger.exception(e);
            throw new GreedyException(e);
        }
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
            response = webb.get((GreedyClient.args.localCallback ? Constants.AUTH0_AUDIENCE_LOCAL :
                    Constants.AUTH0_AUDIENCE) + "api/auth/userinfo")
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
}
