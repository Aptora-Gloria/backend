package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPullRequestsPort {

    List<PullRequestInfo> getPullRequestsForRepository(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
