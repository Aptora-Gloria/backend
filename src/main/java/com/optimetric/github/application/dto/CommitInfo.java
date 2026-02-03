package com.optimetric.github.application.dto;

import java.util.List;

/**
 * Application DTO for Commit information (application layer).
 * Not to be confused with API CommitDto structure; this is minimal for application/domain use.
 */
public record CommitInfo(
        String sha,
        String nodeId,
        String message,
        String authorLogin,
        String committerLogin,
        List<String> parentShas,
        String htmlUrl,
        String apiUrl
) {}
