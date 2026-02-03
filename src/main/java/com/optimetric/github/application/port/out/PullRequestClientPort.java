package com.optimetric.github.application.port.out;

import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;

import java.util.List;

/**
 * Outbound port for fetching Pull Request data from GitHub API.
 */
public interface PullRequestClientPort {
    List<GitHubPullRequestResponse> getPullRequests(String accessToken, String owner, String repo, int page);
    GitHubPullRequestDetailResponse getPullRequestDetails(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestReviewResponse> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestCommitResponse> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber);
}
