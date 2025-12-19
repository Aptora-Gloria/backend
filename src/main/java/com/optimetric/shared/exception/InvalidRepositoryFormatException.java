package com.optimetric.shared.exception;

/**
 * Exception thrown when repository full name format is invalid.
 * Repository full name should be in format "owner/repo".
 */
public class InvalidRepositoryFormatException extends RuntimeException {

    public InvalidRepositoryFormatException(String message) {
        super(message);
    }

    public InvalidRepositoryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}