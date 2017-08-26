package com.rolandoislas.greedygreedy.android.auth;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.badlogic.gdx.Gdx;
import com.rolandoislas.greedygreedy.android.AndroidLauncher;
import com.rolandoislas.greedygreedy.core.auth.AuthenticationHandler;
import com.rolandoislas.greedygreedy.core.auth.AuthenticationHandlerHosted;
import com.rolandoislas.greedygreedy.core.data.Constants;
import com.rolandoislas.greedygreedy.core.util.Logger;

public class AuthenticationHandlerAndroid extends AuthenticationCallback implements AuthenticationHandler {
    private final AndroidLauncher androidLauncher;
    private Runnable loginSuccess;
    private LoginThread loginThread;

    public AuthenticationHandlerAndroid(AndroidLauncher androidLauncher) {
        this.androidLauncher = androidLauncher;
    }

    @Override
    public void login(Runnable loginSuccess, String authCode) {
        this.loginSuccess = loginSuccess;
        if (loginThread != null)
            loginThread.interrupt();
        loginThread = new LoginThread(this);
        loginThread.start();
    }

    @Override
    public void cancel() {

    }

    @Override
    public void login(Runnable loginSuccess) {
        login(loginSuccess, "");
    }

    @Override
    public void logout() {
        AuthenticationHandlerHosted.logoutStatic();
    }

    @Override
    public void onAuthentication(Credentials credentials) {
        Logger.debug("Logged in via Android lock.");
        AuthenticationHandlerHosted.saveCredentials(credentials.getAccessToken(), credentials.getRefreshToken());
        Gdx.app.postRunnable(loginSuccess);
    }

    @Override
    public void onCanceled() {

    }

    @Override
    public void onError(LockException error) {
        Logger.exception(error);
    }

    private class LoginThread extends Thread {
        private final AuthenticationHandlerAndroid authenticationHandlerAndroid;

        private LoginThread(AuthenticationHandlerAndroid authenticationHandlerAndroid) {
            this.authenticationHandlerAndroid = authenticationHandlerAndroid;
        }

        @Override
        public void run() {
            if (AuthenticationHandlerHosted.loginCached()) {
                Gdx.app.postRunnable(loginSuccess);
            }
            else {
                Auth0 auth0 = new Auth0(Constants.AUTH0_CLIENT_ID, Constants.AUTH0_DOMAIN);
                auth0.setOIDCConformant(true);
                Lock lock = Lock.newBuilder(auth0, authenticationHandlerAndroid)
                        .withScheme("com.rolandoislas.greedygreedy")
                        .withAudience(Constants.AUTH0_AUDIENCE)
                        .withScope(Constants.AUTH0_SCOPE)
                        .closable(true)
                        .build(androidLauncher);
                androidLauncher.startActivity(lock.newIntent(androidLauncher));
            }
        }
    }
}
