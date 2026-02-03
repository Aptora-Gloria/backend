package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: get all Pull Requests for a repository.
 * Controller calls this; use case implements it.
 */
public interface GetPullRequestsPort {

    List<PullRequestInfo> getPullRequestsForRepository(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
