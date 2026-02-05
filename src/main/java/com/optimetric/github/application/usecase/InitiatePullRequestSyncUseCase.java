package com.optimetric.github.application.usecase;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.port.in.InitiatePullRequestSyncPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitiatePullRequestSyncUseCase implements InitiatePullRequestSyncPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public void initiatePullRequestSync(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        pullRequestQueryPort.initiatePullRequestSync(login, repoFullName);
    }
}
