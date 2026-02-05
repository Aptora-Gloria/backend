package com.optimetric.github.application.usecase;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.port.in.GetPullRequestPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPullRequestUseCase implements GetPullRequestPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public PullRequestInfo getPullRequest(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        if (prNumber == null) throw new IllegalArgumentException("prNumber must not be null");
        return pullRequestQueryPort.getPullRequest(login, repoFullName, prNumber);
    }
}
