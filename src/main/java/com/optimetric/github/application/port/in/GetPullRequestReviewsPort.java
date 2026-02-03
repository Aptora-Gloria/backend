package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: get reviews for a Pull Request.
 */
public interface GetPullRequestReviewsPort {

    List<ReviewInfo> getPullRequestReviews(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException;
}
