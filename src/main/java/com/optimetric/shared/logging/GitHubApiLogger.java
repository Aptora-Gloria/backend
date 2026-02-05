package com.optimetric.shared.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GitHubApiLogger {

    public void logApiCallStart(String operation, String owner, String repo, Object... params) {
        log.debug("GitHub API call started: {} for {}/{} - Params: {}", 
                 operation, owner, repo, formatParams(params));
    }

    public void logApiCallSuccess(String operation, String owner, String repo, int resultCount, long durationMs) {
        log.debug("GitHub API call completed: {} for {}/{} - Results: {} - Duration: {}ms", 
                 operation, owner, repo, resultCount, durationMs);
    }

    public void logRateLimit(String operation, int remaining, long resetTime) {
        log.info("GitHub API rate limit: {} - Remaining: {} - Reset: {}", 
                operation, remaining, resetTime);
    }

    public void logRateLimitExceeded(String operation, long resetTime) {
        log.warn("GitHub API rate limit exceeded: {} - Reset at: {}", operation, resetTime);
    }

    public void logAuthenticationError(String operation, String owner, String repo) {
        log.error("GitHub API authentication failed: {} for {}/{} - Check access token", 
                 operation, owner, repo);
    }

    public void logPermissionError(String operation, String owner, String repo) {
        log.error("GitHub API permission denied: {} for {}/{} - User may not have access", 
                 operation, owner, repo);
    }

    public void logNotFoundError(String operation, String owner, String repo, Object identifier) {
        log.warn("GitHub API resource not found: {} for {}/{} - Identifier: {}", 
                operation, owner, repo, identifier);
    }

    public void logServerError(String operation, String owner, String repo, int statusCode, String message) {
        log.error("GitHub API server error: {} for {}/{} - Status: {} - Message: {}", 
                 operation, owner, repo, statusCode, message);
    }

    public void logSyncJobStart(String jobType, String repoFullName, Long jobId) {
        log.info("Sync job started: {} for {} - Job ID: {}", jobType, repoFullName, jobId);
    }

    public void logSyncJobComplete(String jobType, String repoFullName, Long jobId, 
                                  int processedCount, long durationMs) {
        log.info("Sync job completed: {} for {} - Job ID: {} - Processed: {} - Duration: {}ms", 
                jobType, repoFullName, jobId, processedCount, durationMs);
    }

    public void logSyncJobFailure(String jobType, String repoFullName, Long jobId, 
                                 String errorMessage, int attemptCount) {
        log.error("Sync job failed: {} for {} - Job ID: {} - Attempt: {} - Error: {}", 
                 jobType, repoFullName, jobId, attemptCount, errorMessage);
    }

    public void logDataProcessing(String operation, String entityType, Object identifier, String action) {
        log.debug("Data processing: {} {} {} - Action: {}", operation, entityType, identifier, action);
    }

    public void logValidationError(String operation, String field, Object value, String reason) {
        log.warn("Validation error: {} - Field: {} - Value: {} - Reason: {}", 
                operation, field, value, reason);
    }

    public void logPerformanceMetric(String operation, String metric, Object value) {
        log.info("Performance metric: {} - {}: {}", operation, metric, value);
    }

    private String formatParams(Object... params) {
        if (params == null || params.length == 0) {
            return "none";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(params[i]);
        }
        return sb.toString();
    }
}