package com.optimetric.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.RepositorySyncStatusRequest;
import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.github.application.port.RepoSyncStatusServicePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for repository sync status endpoints.
 * Provides endpoints to check repository data readiness.
 */
@RestController
@RequestMapping("/api/github/repositories")
@RequiredArgsConstructor
public class RepositorySyncStatusController {

    private final RepoSyncStatusServicePort repoSyncStatusService;

    /**
     * Gets the sync status and data readiness for a repository.
     * 
     * @param request Request containing repository full name
     * @return RepositoryReadinessDto with state, missing components, and flags
     */
    @PostMapping("/sync/status")
    public ResponseEntity<RepositoryReadinessDto> getSyncStatus(
            @RequestBody @Valid RepositorySyncStatusRequest request
    ) {
        RepositoryReadinessDto readiness = repoSyncStatusService.getReadiness(request.fullName());
        return ResponseEntity.ok(readiness);
    }
}
