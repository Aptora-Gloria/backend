package com.optimetric.shared.exception;

public class InvalidRepositoryFormatException extends RuntimeException {

    public InvalidRepositoryFormatException(String message) {
        super(message);
    }

    public InvalidRepositoryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}