package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: get commits for a Pull Request.
 */
public interface GetPullRequestCommitsPort {

    List<ReviewCommentInfo> getPullRequestCommits(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
