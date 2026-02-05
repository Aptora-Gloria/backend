package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPullRequestPort {

    PullRequestInfo getPullRequest(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
