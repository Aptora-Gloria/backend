package com.optimetric.github.api.response;

import java.util.List;

/**
 * Response DTO for pull request metrics synchronization.
 * Contains summary information and detailed lists of commits, reviews, and comments for each PR.
 */
public record SyncMetricsResponse(
        String repositoryFullName,
        int totalPullRequests,
        List<PullRequestMetrics> pullRequests
) {
    /**
     * Metrics and related data for a single pull request.
     */
    public record PullRequestMetrics(
            Integer prNumber,
            String prTitle,
            String prState,
            Integer commitCount,
            Integer reviewCount,
            Integer commentCount,
            List<CommitDto> commits,
            List<PullRequestReviewDto> reviews,
            List<PullRequestCommentDto> comments
    ) {}
}

