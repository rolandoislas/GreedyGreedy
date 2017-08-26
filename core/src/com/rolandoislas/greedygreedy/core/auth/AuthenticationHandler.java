package com.rolandoislas.greedygreedy.core.auth;

public interface AuthenticationHandler {
    void login(Runnable runnable, String authCode);

    void cancel();

    void login(Runnable runnable);

    void logout();
}
