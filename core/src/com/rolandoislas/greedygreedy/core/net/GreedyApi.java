package com.rolandoislas.greedygreedy.core.net;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class GreedyApi {

    public static boolean confirmToken(String accessToken) {
        Webb webb = Webb.create();
        Response<JSONObject> response;
        try {
            response = webb.get((GreedyClient.args.localCallback ? Constants.AUTH0_AUDIENCE_LOCAL :
                    Constants.AUTH0_AUDIENCE) + "api/auth/token/verify")
                    .header("Authentication", "Bearer " + accessToken).ensureSuccess().asJsonObject();
        }
        catch (WebbException e) {
            Logger.exception(e);
            return false;
        }
        try {
            return response.getBody().getBoolean("valid");
        } catch (JSONException e) {
            Logger.exception(e);
            return false;
        }
    }
}
