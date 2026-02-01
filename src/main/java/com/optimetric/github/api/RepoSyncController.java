package com.optimetric.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.ReadinessRequest;
import com.optimetric.github.api.request.TriggerSyncRequest;
import com.optimetric.github.api.response.ReadinessResponse;
import com.optimetric.github.api.response.TriggerSyncResponse;
import com.optimetric.github.application.dto.TriggerSyncCommand;
import com.optimetric.github.application.dto.TriggerSyncResult;
import com.optimetric.github.application.usecase.GetRepoReadinessUseCase;
import com.optimetric.github.application.usecase.TriggerSyncForRepoUseCase;
import com.optimetric.shared.exception.RepositoryNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Repo Sync REST API: tetikleme ve hazırlık.
 * Hexagonal: API (driving) katmanı; use case'lere delegasyon.
 */
@RestController
@RequestMapping("/api/reposync")
@RequiredArgsConstructor
@Slf4j
public class RepoSyncController {

    private final TriggerSyncForRepoUseCase triggerSyncForRepoUseCase;
    private final GetRepoReadinessUseCase getRepoReadinessUseCase;

    /**
     * Seçilen repo için sync tetikler; eksikse job'ları kuyruğa alır.
     */
    @PostMapping("/trigger")
    public ResponseEntity<TriggerSyncResponse> trigger(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid TriggerSyncRequest request
    ) {
        TriggerSyncResult result = triggerSyncForRepoUseCase.execute(
                new TriggerSyncCommand(login, request.fullName())
        );
        if (result.status() == TriggerSyncResult.TriggerSyncStatus.REPO_NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(TriggerSyncResponse.from(result));
    }

    /**
     * Seçilen repo için hazırlık durumunu döndürür (state, missing, flags).
     */
    @PostMapping("/readiness")
    public ResponseEntity<ReadinessResponse> readiness(
            @RequestBody @Valid ReadinessRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    ReadinessResponse.from(getRepoReadinessUseCase.execute(request.fullName()))
            );
        } catch (RepositoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
