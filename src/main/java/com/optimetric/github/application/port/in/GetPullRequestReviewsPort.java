package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPullRequestReviewsPort {

    List<ReviewInfo> getPullRequestReviews(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
