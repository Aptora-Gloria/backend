package com.optimetric.github.application.port.out;

import java.util.List;

import com.optimetric.github.application.dto.PullRequestCommitFromApi;
import com.optimetric.github.application.dto.PullRequestDetailFromApi;
import com.optimetric.github.application.dto.PullRequestFromApi;
import com.optimetric.github.application.dto.PullRequestReviewFromApi;

public interface PullRequestClientPort {
    List<PullRequestFromApi> getPullRequests(String accessToken, String owner, String repo, int page);
    PullRequestDetailFromApi getPullRequestDetails(String accessToken, String owner, String repo, int prNumber);
    List<PullRequestReviewFromApi> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber);
    List<PullRequestCommitFromApi> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber);
}
