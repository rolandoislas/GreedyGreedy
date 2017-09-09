package com.rolandoislas.greedygreedy.core.util;

public class GreedyException extends Exception {
    public GreedyException(Throwable e) {
        super(e);
    }

    public GreedyException(String message) {
        super(message);
    }

    public GreedyException() {
        super();
    }
}
