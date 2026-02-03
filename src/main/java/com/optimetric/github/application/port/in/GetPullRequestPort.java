package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Inbound port: get a single Pull Request by repo and PR number.
 */
public interface GetPullRequestPort {

    PullRequestInfo getPullRequest(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
