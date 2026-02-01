package com.optimetric.github.application.usecase;

import org.springframework.stereotype.Service;

import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.github.application.port.RepoSyncStatusServicePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Repo hazırlık bilgisi use case'i.
 * Application port (RepoSyncStatusServicePort) üzerinden hexagon içinde kalır.
 */
@Service
@RequiredArgsConstructor
public class GetRepoReadinessUseCase {

    private final RepoSyncStatusServicePort repoSyncStatusServicePort;

    public RepositoryReadinessDto execute(String fullName) throws RepositoryNotFoundException {
        return repoSyncStatusServicePort.getReadiness(fullName);
    }
}
