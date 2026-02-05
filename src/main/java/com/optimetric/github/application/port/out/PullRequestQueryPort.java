package com.optimetric.github.application.port.out;

import java.util.List;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface PullRequestQueryPort {

    List<PullRequestInfo> getPullRequestsForRepository(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;

    PullRequestInfo getPullRequest(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;

    List<ReviewInfo> getPullRequestReviews(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;

    List<ReviewCommentInfo> getPullRequestCommits(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;

    void initiatePullRequestSync(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;

    SyncStatusInfo getSyncStatus(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
