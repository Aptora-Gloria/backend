package com.optimetric.github.application.port.in;

import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface InitiatePullRequestSyncPort {

    void initiatePullRequestSync(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
