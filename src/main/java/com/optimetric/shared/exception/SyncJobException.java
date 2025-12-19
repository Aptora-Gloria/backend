package com.optimetric.shared.exception;

/**
 * Exception thrown when synchronization job operations fail.
 * This includes job creation, execution, and status management failures.
 */
public class SyncJobException extends RuntimeException {

    public SyncJobException(String message) {
        super(message);
    }

    public SyncJobException(String message, Throwable cause) {
        super(message, cause);
    }
}