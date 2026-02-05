package com.optimetric.shared.exception;

public class SyncJobException extends RuntimeException {

    public SyncJobException(String message) {
        super(message);
    }

    public SyncJobException(String message, Throwable cause) {
        super(message, cause);
    }
}