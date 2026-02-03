package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.RepositoryReadinessInfo;
import com.optimetric.github.application.port.in.GetRepoSyncStatusPort;
import com.optimetric.github.application.port.out.RepoSyncStatusQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case: get repository sync/readiness status.
 */
@Service
@RequiredArgsConstructor
public class GetRepoSyncStatusUseCase implements GetRepoSyncStatusPort {

    private final RepoSyncStatusQueryPort repoSyncStatusQueryPort;

    @Override
    public RepositoryReadinessInfo getReadiness(String fullName) throws RepositoryNotFoundException {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName must not be blank");
        return repoSyncStatusQueryPort.getReadiness(fullName);
    }
}
