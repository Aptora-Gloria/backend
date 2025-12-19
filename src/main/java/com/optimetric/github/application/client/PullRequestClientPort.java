package com.optimetric.github.application.client;

import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;

import java.util.List;

public interface PullRequestClientPort {
    List<GitHubPullRequestResponse> getPullRequests(String accessToken, String owner, String repo, int page);
    GitHubPullRequestDetailResponse getPullRequestDetails(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestReviewResponse> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestCommitResponse> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber);
}