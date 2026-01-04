package com.optimetric.shared.exception;

public class BranchNotFoundException extends RuntimeException {

    public BranchNotFoundException(String message) {
        super(message);
    }

    public BranchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}



