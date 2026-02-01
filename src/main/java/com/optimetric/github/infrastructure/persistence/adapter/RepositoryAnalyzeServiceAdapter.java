package com.optimetric.github.infrastructure.persistence.adapter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.github.application.dto.AnalyzeResult;
import com.optimetric.github.application.port.BranchServicePort;
import com.optimetric.github.application.port.CommitServicePort;
import com.optimetric.github.application.port.RepoSyncStatusServicePort;
import com.optimetric.github.application.port.RepositoryAnalyzeServicePort;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.domain.enumeration.ReadinessState;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;
import com.optimetric.github.infrastructure.persistence.jpa.SyncJobRepository;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Repo hazırlık analizi ve eksik bileşenler için sync tetikleyen adapter.
 * READY değilse: önce branch ve commit sync (doğrudan servis çağrısı), sonra PR_LIST job oluşturulup
 * hemen çalıştırılır (PR listesi, detaylar ve yorumlar senkronize edilir).
 * RUNNING job varsa SYNC_ALREADY_RUNNING döner. Sadece PENDING job varsa ve PR hazır değilse
 * mevcut PR_LIST job çalıştırılarak takılı job kurtarılır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryAnalyzeServiceAdapter implements RepositoryAnalyzeServicePort {

    private final RepoSyncStatusServicePort repoSyncStatusService;
    private final SyncJobServicePort syncJobServicePort;
    private final SyncJobRepository syncJobRepository;
    private final BranchServicePort branchServicePort;
    private final CommitServicePort commitServicePort;

    @Override
    @Transactional
    public AnalyzeResult analyze(String login, String fullName) throws RepositoryNotFoundException, UserNotFoundException {
        var readiness = repoSyncStatusService.getReadiness(fullName);

        if (readiness.state() == ReadinessState.READY) {
            log.info("Repository {} already ready, no jobs enqueued", fullName);
            return new AnalyzeResult(AnalyzeResult.AnalyzeState.ALREADY_READY);
        }

        boolean hasPending = syncJobRepository.countByRepoFullNameAndStatus(fullName, SyncJobStatus.PENDING) > 0;
        boolean hasRunning = syncJobRepository.countByRepoFullNameAndStatus(fullName, SyncJobStatus.RUNNING) > 0;
        if (hasRunning) {
            log.info("Repository {} has RUNNING sync jobs, skipping enqueue", fullName);
            return new AnalyzeResult(AnalyzeResult.AnalyzeState.SYNC_ALREADY_RUNNING);
        }
        if (hasPending && !readiness.flags().pullRequestsReady()) {
            log.info("Repository {} has PENDING PR job(s), executing PR_LIST job", fullName);
            syncJobServicePort.executePrListJob(fullName);
            return new AnalyzeResult(AnalyzeResult.AnalyzeState.SYNC_STARTED);
        }
        if (hasPending) {
            log.info("Repository {} has active sync jobs, skipping enqueue", fullName);
            return new AnalyzeResult(AnalyzeResult.AnalyzeState.SYNC_ALREADY_RUNNING);
        }

        if (!readiness.flags().branchesReady()) {
            branchServicePort.initRepoBranches(login, fullName);
            log.info("Synced branches for repository {}", fullName);
        }

        if (!readiness.flags().commitsReady()) {
            var branches = branchServicePort.getBranchesByFullName(login, fullName);
            if (branches.isEmpty()) {
                log.warn("No branches found for repository {}, skipping commit sync", fullName);
            } else {
                for (var branch : branches) {
                    String branchName = branch.name();
                    if (branchName == null || branchName.isBlank()) continue;
                    try {
                        commitServicePort.initCommits(login, fullName, branchName, null);
                        log.debug("Synced commits for repository {} (branch: {})", fullName, branchName);
                    } catch (BranchNotFoundException e) {
                        log.warn("Could not sync commits for repository {} branch {}: {}", fullName, branchName, e.getMessage());
                    }
                }
                log.info("Synced commits for repository {} ({} branches)", fullName, branches.size());
            }
        }

        if (!readiness.flags().pullRequestsReady()) {
            syncJobServicePort.createJob(SyncJobType.PR_LIST, fullName);
            syncJobServicePort.executePrListJob(fullName);
            log.info("PR_LIST job executed for repository {}", fullName);
        }

        return new AnalyzeResult(AnalyzeResult.AnalyzeState.SYNC_STARTED);
    }
}
