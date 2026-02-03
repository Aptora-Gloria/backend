package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.port.in.GetPullRequestsPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case: get all Pull Requests for a repository.
 * Validates input and delegates to PullRequestQueryPort.
 */
@Service
@RequiredArgsConstructor
public class GetPullRequestsUseCase implements GetPullRequestsPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public List<PullRequestInfo> getPullRequestsForRepository(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName must not be blank");
        }
        return pullRequestQueryPort.getPullRequestsForRepository(login, repoFullName);
    }
}
