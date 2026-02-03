package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.port.in.GetPullRequestReviewsPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case: get reviews for a Pull Request.
 */
@Service
@RequiredArgsConstructor
public class GetPullRequestReviewsUseCase implements GetPullRequestReviewsPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public List<ReviewInfo> getPullRequestReviews(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        if (prNumber == null) throw new IllegalArgumentException("prNumber must not be null");
        return pullRequestQueryPort.getPullRequestReviews(login, repoFullName, prNumber);
    }
}
