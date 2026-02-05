package com.optimetric.shared.exception;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }

    public PullRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}