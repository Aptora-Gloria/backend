package com.optimetric.github.application.usecase;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.dto.AnalyzeResult;
import com.optimetric.github.application.dto.TriggerSyncCommand;
import com.optimetric.github.application.dto.TriggerSyncResult;
import com.optimetric.github.application.port.RepositoryAnalyzeServicePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seçilen repo için sync tetikleme use case'i.
 * Application port (RepositoryAnalyzeServicePort) üzerinden hexagon içinde kalır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TriggerSyncForRepoUseCase {

    private final RepositoryAnalyzeServicePort repositoryAnalyzeServicePort;

    public TriggerSyncResult execute(TriggerSyncCommand command) {
        try {
            AnalyzeResult result = repositoryAnalyzeServicePort.analyze(command.login(), command.fullName());
            return switch (result.state()) {
                case SYNC_STARTED -> TriggerSyncResult.triggered("Sync job'ları kuyruğa alındı.");
                case ALREADY_READY -> TriggerSyncResult.alreadyReady();
                case SYNC_ALREADY_RUNNING -> TriggerSyncResult.syncAlreadyRunning();
            };
        } catch (RepositoryNotFoundException e) {
            log.warn("Repo not found for trigger: {}", command.fullName(), e);
            return TriggerSyncResult.repoNotFound(e.getMessage());
        }
    }
}
