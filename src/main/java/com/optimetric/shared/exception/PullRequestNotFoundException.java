package com.optimetric.shared.exception;

/**
 * Exception thrown when a requested Pull Request is not found.
 * This can occur when:
 * - PR doesn't exist in the database
 * - PR exists on GitHub but hasn't been ingested yet
 * - User doesn't have access to the PR's repository
 */
public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }

    public PullRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}