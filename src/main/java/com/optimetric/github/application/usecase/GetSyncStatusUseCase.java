package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.application.port.in.GetSyncStatusPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case: get sync status for a repository.
 */
@Service
@RequiredArgsConstructor
public class GetSyncStatusUseCase implements GetSyncStatusPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public SyncStatusInfo getSyncStatus(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        return pullRequestQueryPort.getSyncStatus(login, repoFullName);
    }
}
