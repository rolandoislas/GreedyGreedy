package com.rolandoislas.greedygreedy.server.util;

import com.rolandoislas.greedygreedy.core.util.Logger;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

public class LogAndDieExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Exception exception, Request request, Response response) {
        Logger.exception(exception);
        Logger.warn("Unhandled exception. Server is halting.");
        System.exit(1);
    }
}
