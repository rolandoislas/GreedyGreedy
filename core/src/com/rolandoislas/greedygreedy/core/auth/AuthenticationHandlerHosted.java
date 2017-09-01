package com.rolandoislas.greedygreedy.core.auth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.JsonObject;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.net.GreedyApi;
import com.rolandoislas.greedygreedy.core.net.LoginCallbackHandler;
import com.rolandoislas.greedygreedy.core.stage.StageMenu;
import com.rolandoislas.greedygreedy.core.util.GreedyException;
import com.rolandoislas.greedygreedy.core.util.Logger;
import com.rolandoislas.greedygreedy.core.util.PreferencesUtil;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

public class AuthenticationHandlerHosted implements AuthenticationHandler {
    private static LoginThread loginThread;

    private static void showLoginPage() {
        // Create code challenge
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        String verifier = base64Encode(code);
        byte[] bytes;
        try {
            bytes = verifier.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            Logger.exception(e);
            return;
        }
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        preferences.putString(Constants.PREF_CODE_VERIFIER, verifier);
        preferences.flush();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Logger.exception(e);
            return;
        }
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();
        String codeChallenge = base64Encode(digest);
        // Construct URL
        String responseType = "code";
        String codeChallengeMethod = "S256";
        String loginUri = String.format(Locale.US,
                "%sauthorize?audience=%s&scope=%s&response_type=%s&client_id=%s&code_challenge=%s&" +
                        "code_challenge_method=%s&redirect_uri=%s",
                Constants.AUTH0_DOMAIN, Constants.AUTH0_AUDIENCE, Constants.AUTH0_SCOPE, responseType,
                Constants.AUTH0_CLIENT_ID, codeChallenge, codeChallengeMethod, getRedirectUrl());
        Gdx.net.openURI(loginUri);
    }

    private static String getRedirectUrl() {
        String redirectUrl = GreedyClient.args.localCallback ? Constants.AUTH0_REDIRECT_URL_LOCAL :
                Constants.AUTH0_REDIRECT_URL;
        switch (Gdx.app.getType()) {
            case Android:
                redirectUrl += "?type=android";
                break;
            case Desktop:
                redirectUrl += "?type=desktop";
                break;
        }
        return redirectUrl;
    }

    private static String base64Encode(byte[] bytes) {
        String encodedString = new String(Base64.encodeBase64(bytes));
        return encodedString.replace('+','-').replace('/','_').replace("=", "");
    }

    public void login(Runnable loginSuccess, String authCode) {
        if (AuthenticationHandlerHosted.loginThread != null)
            AuthenticationHandlerHosted.loginThread.interrupt();
        AuthenticationHandlerHosted.loginThread = new LoginThread(loginSuccess, authCode);
        AuthenticationHandlerHosted.loginThread.start();
    }

    public void login(Runnable loginSuccess) {
        login(loginSuccess, "");
    }

    public void logout() {
        AuthenticationHandlerHosted.logoutStatic();
    }

    public static void logoutStatic() {
        Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        if (preferences.contains(Constants.PREF_ACCESS_TOKEN))
            preferences.remove(Constants.PREF_ACCESS_TOKEN);
        if (preferences.contains(Constants.PREF_REFRESH_TOKEN))
            preferences.remove(Constants.PREF_REFRESH_TOKEN);
        if (preferences.contains(Constants.PREF_USER_INFO))
            preferences.remove(Constants.PREF_USER_INFO);
        preferences.flush();
    }

    public void cancel() {
        if (loginThread != null)
            loginThread.interrupt();
    }

    public static void saveCredentials(String accessToken, String refreshToken) {
        final Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
        preferences.putString(Constants.PREF_ACCESS_TOKEN, accessToken);
        if (refreshToken != null && !refreshToken.isEmpty())
            preferences.putString(Constants.PREF_REFRESH_TOKEN, refreshToken);
        String userInfo = null;
        try {
            userInfo = GreedyApi.getUserInfo();
        } catch (GreedyException e) {
            Logger.exception(e);
        }
        if (userInfo != null && !userInfo.isEmpty())
            preferences.putString(Constants.PREF_USER_INFO, userInfo);
        preferences.flush();
    }

    public static boolean loginCached() {
        if (AuthenticationHandlerHosted.loginThread != null)
            AuthenticationHandlerHosted.loginThread.interrupt();
        AuthenticationHandlerHosted.loginThread = new LoginThread(null, "");
        AuthenticationHandlerHosted.loginThread.setOnlyRefresh(true);
        AuthenticationHandlerHosted.loginThread.start();
        while (AuthenticationHandlerHosted.loginThread.isAlive()) {}
        return !AuthenticationHandlerHosted.loginThread.getNeedsLogin();
    }

    private static class LoginThread extends Thread {
        private final Runnable loginSuccess;
        private final String authCode;
        private LoginCallbackHandler loginCallbackHandler;
        private boolean onlyRefresh;
        private boolean needsLogin;

        LoginThread(Runnable loginSuccess, String authCode) {
            this.loginSuccess = loginSuccess;
            this.authCode = authCode;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            try {
                if (loginCallbackHandler != null)
                    loginCallbackHandler.stop();
            } catch (IOException e) {
                Logger.exception(e);
            } catch (InterruptedException e) {
                Logger.exception(e);
            }
        }

        @Override
        public void run() {
            Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
            // New auth code. Request new token
            if (!authCode.isEmpty()) {
                requestToken();
                return;
            }
            // Check a cached token
            if (PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL).contains(Constants.PREF_ACCESS_TOKEN)) {
                try {
                    if (GreedyApi.confirmToken()) {
                        if (!onlyRefresh)
                            Gdx.app.postRunnable(loginSuccess);
                        return;
                    }
                } catch (GreedyException e) {
                    Logger.exception(e);
                    fail("API connection failed");
                    return;
                }
            }
            // Use a refresh token
            if (preferences.contains(Constants.PREF_REFRESH_TOKEN)) {
                preferences.remove(Constants.PREF_ACCESS_TOKEN);
                preferences.flush();
                requestToken();
                return;
            }
            // New login
            setNeedsLogin(true);
            if (!onlyRefresh) {
                showLoginPage();
                loginCallbackHandler = new LoginCallbackHandler(new InetSocketAddress(Constants.LOGIN_CALLBACK_PORT));
                loginCallbackHandler.listen();
            }
        }

        private void requestToken() {
            Preferences preferences = PreferencesUtil.get(Constants.PREF_CATEGORY_GENERAL);
            // Request
            JsonObject json = new JsonObject();
            json.addProperty("client_id", Constants.AUTH0_CLIENT_ID);
            // New token request
            if (!authCode.isEmpty()) {
                json.addProperty("grant_type", "authorization_code");
                json.addProperty("code_verifier", preferences.getString(Constants.PREF_CODE_VERIFIER));
                json.addProperty("code", authCode);
                json.addProperty("redirect_uri", getRedirectUrl());
            }
            // Refresh the token
            else if (preferences.contains(Constants.PREF_REFRESH_TOKEN)) {
                json.addProperty("grant_type", "refresh_token");
                json.addProperty("refresh_token", preferences.getString(Constants.PREF_REFRESH_TOKEN));
            }
            // No auth code or refresh token
            else {
                fail("Unknown error");
                return;
            }
            // Perform the request
            Webb webb = Webb.create();
            Response<JSONObject> response = null;
            try {
                response = webb.post(Constants.AUTH0_DOMAIN + "oauth/token")
                        .header("Content-Type", "application/json")
                        .body(json.toString()).ensureSuccess().asJsonObject();
            }
            catch (WebbException ignore) {}
            // No access token returned
            if (response == null || !response.getBody().has("access_token")) {
                // Refresh token failed. Remove it
                // /me wags their finger at the refresh token and scolds them with a "Bad refresh token. Bad."
                if (authCode.isEmpty() && preferences.contains(Constants.PREF_REFRESH_TOKEN))
                    preferences.remove(Constants.PREF_REFRESH_TOKEN);
                // Remove any cached access token that may be there
                if (preferences.contains(Constants.PREF_ACCESS_TOKEN))
                    preferences.remove(Constants.PREF_ACCESS_TOKEN);
                preferences.flush();
                fail("Failed to authenticate");
                return;
            }
            // Save the token and initiate the login callback
            try {
                saveCredentials(response.getBody().getString("access_token"),
                        response.getBody().has("refresh_token") ?
                                response.getBody().getString("refresh_token") : "");
            }
            catch (JSONException e) {
                Logger.exception(e);
                fail("Auth server error");
                return;
            }
            if (!onlyRefresh)
                Gdx.app.postRunnable(loginSuccess);
        }

        private void fail(final String message) {
            if (onlyRefresh)
                return;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GreedyClient.setStage(new StageMenu(message));
                }
            });
        }

        private void setOnlyRefresh(boolean onlyRefresh) {
            this.onlyRefresh = onlyRefresh;
        }

        private void setNeedsLogin(boolean needsLogin) {
            this.needsLogin = needsLogin;
        }

        private boolean getNeedsLogin() {
            return needsLogin;
        }
    }
}
