package com.rolandoislas.greedygreedy.server.util;

import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.server.impl.PurchaseVerifierAndroidGoogle;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class GreedyPurchaseVerifierAndroidGoogle extends PurchaseVerifierAndroidGoogle {
    private String accessToken;
    private long accessTokenExpireTime;

    @Override
    public boolean isValid(Transaction transaction) {
        String url = String.format(Locale.US, "https://www.googleapis.com/androidpublisher/v2/applications/%s" +
                "/purchases/products/%s/tokens/%s",
                "com.rolandoislas.greedygreedy", // Package
                        transaction.getIdentifier(), // product id
                        transaction.getTransactionData() // transaction token
        );
        Webb webb = Webb.create();
        try {
            Response<JSONObject> response = webb.get(url)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .ensureSuccess().asJsonObject();
            if (response.getBody().getInt("purchaseState") == 0)
                return true;
        }
        catch (WebbException | GreedyException | JSONException e) {
            Logger.exception(e);
            return false;
        }
        return false;
    }

    private String getAccessToken() throws WebbException, GreedyException {
        if (accessToken == null || System.currentTimeMillis() + 60 * 1000 >= accessTokenExpireTime) {
            Webb webb = Webb.create();
            HashMap<String, Object> params = new HashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("client_id", System.getenv("GOOGLE_API_CLIENT_ID"));
            params.put("client_secret", System.getenv("GOOGLE_API_CLIENT_SECRET"));
            params.put("refresh_token", System.getenv("GOOGLE_API_REFRESH_TOKEN"));
            Response<JSONObject> response = webb.post("https://accounts.google.com/o/oauth2/token")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("accept", "application/json")
                    .params(params)
                    .ensureSuccess().asJsonObject();
            try {
                accessToken = response.getBody().getString("access_token");
                accessTokenExpireTime = System.currentTimeMillis() +
                        response.getBody().getInt("expires_in") * 1000;
            } catch (JSONException e) {
                Logger.exception(e);
                throw new GreedyException("Unexpected response");
            }
        }
        return accessToken;
    }
}
