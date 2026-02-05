package com.optimetric.shared.exception;

import java.time.OffsetDateTime;

public class GitHubRateLimitException extends GitHubApiException {
    
    private final OffsetDateTime resetTime;
    private final int remainingRequests;

    public GitHubRateLimitException(String message, OffsetDateTime resetTime, int remainingRequests) {
        super(message);
        this.resetTime = resetTime;
        this.remainingRequests = remainingRequests;
    }

    public GitHubRateLimitException(String message, Throwable cause, OffsetDateTime resetTime, int remainingRequests) {
        super(message, cause);
        this.resetTime = resetTime;
        this.remainingRequests = remainingRequests;
    }

    public OffsetDateTime getResetTime() {
        return resetTime;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }
}