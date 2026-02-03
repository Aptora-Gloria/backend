package com.optimetric.github.application.port.in;

import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Inbound port: initiate Pull Request sync for a repository.
 */
public interface InitiatePullRequestSyncPort {

    void initiatePullRequestSync(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
