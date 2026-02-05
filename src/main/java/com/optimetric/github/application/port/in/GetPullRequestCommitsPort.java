package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPullRequestCommitsPort {

    List<ReviewCommentInfo> getPullRequestCommits(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
