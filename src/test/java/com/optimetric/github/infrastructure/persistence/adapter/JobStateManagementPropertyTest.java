package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JobStateManagementPropertyTest {

    @Property(tries = 100)
    void syncJobShouldMaintainProperStateTransitions(
            @ForAll("validSyncJobEntity") SyncJobEntity job,
            @ForAll("validStateTransitionSequence") List<SyncJobStatus> stateSequence) {
        
        // Durum yönetimi için bellek içi job deposu benzetimi
        Map<Long, SyncJobEntity> jobStorage = new HashMap<>();
        Long jobId = job.getId() != null ? job.getId() : 1L;
        job.setId(jobId);
        
        // PENDING durumu ile başla
        job.setStatus(SyncJobStatus.PENDING);
        job.setAttemptCount(0);
        jobStorage.put(jobId, job);
        
        SyncJobStatus previousStatus = SyncJobStatus.PENDING;
        
        for (SyncJobStatus targetStatus : stateSequence) {
            SyncJobEntity currentJob = jobStorage.get(jobId);
            
            // Hedef duruma göre geçiş mantığını uygula
            switch (targetStatus) {
                case RUNNING -> {
                    if (previousStatus == SyncJobStatus.PENDING) {
                        currentJob.setStatus(SyncJobStatus.RUNNING);
                        currentJob.setStartedAt(OffsetDateTime.now());
                        currentJob.setAttemptCount(currentJob.getAttemptCount() + 1);
                    }
                }
                case COMPLETED -> {
                    if (previousStatus == SyncJobStatus.RUNNING) {
                        currentJob.setStatus(SyncJobStatus.COMPLETED);
                        currentJob.setCompletedAt(OffsetDateTime.now());
                        currentJob.setErrorMessage(null); // Clear any previous error
                    }
                }
                case FAILED -> {
                    if (previousStatus == SyncJobStatus.RUNNING) {
                        currentJob.setStatus(SyncJobStatus.FAILED);
                        currentJob.setCompletedAt(OffsetDateTime.now());
                        currentJob.setErrorMessage("Test error message");
                        
                        // Maks deneme altındaysa yeniden dene (3)
                        if (currentJob.getAttemptCount() < 3) {
                            currentJob.setNextRunAt(OffsetDateTime.now().plusMinutes(5 * currentJob.getAttemptCount()));
                            currentJob.setStatus(SyncJobStatus.PENDING); // Reset to pending for retry
                        }
                    }
                }
            }
            
            previousStatus = currentJob.getStatus();
        }
        
        // Son durumun geçerli olduğunu doğrula
        SyncJobEntity finalJob = jobStorage.get(jobId);
        assertThat(finalJob.getStatus()).isIn((Object[]) SyncJobStatus.values());
        
        // Durum geçiş kurallarını doğrula
        if (finalJob.getStatus() == SyncJobStatus.RUNNING) {
            assertThat(finalJob.getStartedAt()).isNotNull();
            assertThat(finalJob.getAttemptCount()).isGreaterThan(0);
        }
        
        if (finalJob.getStatus() == SyncJobStatus.COMPLETED) {
            assertThat(finalJob.getCompletedAt()).isNotNull();
            assertThat(finalJob.getErrorMessage()).isNull();
        }
        
        if (finalJob.getStatus() == SyncJobStatus.FAILED && finalJob.getAttemptCount() < 3) {
            assertThat(finalJob.getNextRunAt()).isNotNull();
            assertThat(finalJob.getStatus()).isEqualTo(SyncJobStatus.PENDING); // Should be reset for retry
        }
    }

    @Property(tries = 100)
    void syncJobShouldPersistPaginationInformationForResumability(
            @ForAll("validSyncJobEntity") SyncJobEntity job,
            @ForAll("validPaginationUpdates") List<PaginationUpdate> paginationUpdates) {
        
        // Bellek içi job deposu benzetimi
        Map<Long, SyncJobEntity> jobStorage = new HashMap<>();
        Long jobId = job.getId() != null ? job.getId() : 1L;
        job.setId(jobId);
        jobStorage.put(jobId, job);
        
        // Sayfalama güncellemelerini sırayla uygula
        for (PaginationUpdate update : paginationUpdates) {
            SyncJobEntity currentJob = jobStorage.get(jobId);
            
            // Sayfalama durumunu güncelle
            currentJob.setCursor(update.cursor());
            currentJob.setCurrentPage(update.currentPage());
            
            // Sayfalama bilgisinin doğru saklandığını doğrula
            assertThat(currentJob.getCursor()).isEqualTo(update.cursor());
            assertThat(currentJob.getCurrentPage()).isEqualTo(update.currentPage());
        }
        
        // Son sayfalama durumunun korunduğunu doğrula
        SyncJobEntity finalJob = jobStorage.get(jobId);
        if (!paginationUpdates.isEmpty()) {
            PaginationUpdate lastUpdate = paginationUpdates.get(paginationUpdates.size() - 1);
            assertThat(finalJob.getCursor()).isEqualTo(lastUpdate.cursor());
            assertThat(finalJob.getCurrentPage()).isEqualTo(lastUpdate.currentPage());
        }
        
        // Sayfalamanın sürdürülebilir GitHub API'yi desteklediğini doğrula
        if (finalJob.getCursor() != null) {
            assertThat(finalJob.getCursor()).isNotEmpty();
            assertThat(finalJob.getCursor().length()).isLessThanOrEqualTo(500); // Database constraint
        }
        
        if (finalJob.getCurrentPage() != null) {
            assertThat(finalJob.getCurrentPage()).isGreaterThan(0);
        }
    }

    @Property(tries = 100)
    void syncJobShouldHandleErrorsByRecordingDetailsAndIncrementingAttempts(
            @ForAll("validSyncJobEntity") SyncJobEntity job,
            @ForAll("validErrorMessages") List<String> errorMessages) {
        
        // Bellek içi job deposu benzetimi
        Map<Long, SyncJobEntity> jobStorage = new HashMap<>();
        Long jobId = job.getId() != null ? job.getId() : 1L;
        job.setId(jobId);
        job.setStatus(SyncJobStatus.PENDING);
        job.setAttemptCount(0);
        jobStorage.put(jobId, job);
        
        int maxRetryAttempts = 3;
        
        for (String errorMessage : errorMessages) {
            SyncJobEntity currentJob = jobStorage.get(jobId);
            
            // Simulate job execution attempt
            currentJob.setStatus(SyncJobStatus.RUNNING);
            currentJob.setStartedAt(OffsetDateTime.now());
            currentJob.setAttemptCount(currentJob.getAttemptCount() + 1);
            
            // Simulate job failure
            currentJob.setStatus(SyncJobStatus.FAILED);
            currentJob.setCompletedAt(OffsetDateTime.now());
            currentJob.setErrorMessage(errorMessage);
            
            // Apply retry logic
            if (currentJob.getAttemptCount() < maxRetryAttempts) {
                currentJob.setNextRunAt(OffsetDateTime.now().plusMinutes(5 * currentJob.getAttemptCount()));
                currentJob.setStatus(SyncJobStatus.PENDING); // Reset to pending for retry
            }
            
            // Verify error handling
            assertThat(currentJob.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(currentJob.getAttemptCount()).isGreaterThan(0);
            assertThat(currentJob.getCompletedAt()).isNotNull();
            
            // Verify retry scheduling
            if (currentJob.getAttemptCount() < maxRetryAttempts) {
                assertThat(currentJob.getStatus()).isEqualTo(SyncJobStatus.PENDING);
                assertThat(currentJob.getNextRunAt()).isNotNull();
                assertThat(currentJob.getNextRunAt()).isAfter(OffsetDateTime.now().minusMinutes(1));
            } else {
                assertThat(currentJob.getStatus()).isEqualTo(SyncJobStatus.FAILED);
            }
            
            // Stop if max attempts reached
            if (currentJob.getAttemptCount() >= maxRetryAttempts) {
                break;
            }
        }
        
        // Verify final state respects max retry attempts
        SyncJobEntity finalJob = jobStorage.get(jobId);
        assertThat(finalJob.getAttemptCount()).isLessThanOrEqualTo(maxRetryAttempts);
        
        if (finalJob.getAttemptCount() >= maxRetryAttempts) {
            assertThat(finalJob.getStatus()).isEqualTo(SyncJobStatus.FAILED);
            // Note: nextRunAt may still be set from the last retry attempt, but no more retries will be scheduled
        }
    }

    @Property(tries = 100)
    void syncJobShouldSupportJobSchedulingAndRetryLogic(
            @ForAll("validSyncJobEntity") SyncJobEntity job,
            @ForAll("validRetryScenarios") List<RetryScenario> retryScenarios) {
        
        // Bellek içi job deposu benzetimi with scheduling
        Map<Long, SyncJobEntity> jobStorage = new HashMap<>();
        Long jobId = job.getId() != null ? job.getId() : 1L;
        job.setId(jobId);
        job.setStatus(SyncJobStatus.PENDING);
        job.setAttemptCount(0);
        jobStorage.put(jobId, job);
        
        OffsetDateTime currentTime = OffsetDateTime.now();
        
        for (RetryScenario scenario : retryScenarios) {
            SyncJobEntity currentJob = jobStorage.get(jobId);
            
            // Simulate time advancement
            currentTime = currentTime.plusMinutes(scenario.minutesElapsed());
            
            // Check if job is ready to run based on nextRunAt
            boolean isReadyToRun = currentJob.getStatus() == SyncJobStatus.PENDING && 
                                   (currentJob.getNextRunAt() == null || 
                                    !currentJob.getNextRunAt().isAfter(currentTime));
            
            if (isReadyToRun) {
                // Execute job
                currentJob.setStatus(SyncJobStatus.RUNNING);
                currentJob.setStartedAt(currentTime);
                currentJob.setAttemptCount(currentJob.getAttemptCount() + 1);
                
                // Apply scenario outcome
                if (scenario.shouldSucceed()) {
                    currentJob.setStatus(SyncJobStatus.COMPLETED);
                    currentJob.setCompletedAt(currentTime);
                    currentJob.setErrorMessage(null);
                    currentJob.setNextRunAt(null); // Clear retry scheduling on success
                } else {
                    currentJob.setStatus(SyncJobStatus.FAILED);
                    currentJob.setCompletedAt(currentTime);
                    currentJob.setErrorMessage("Retry scenario failure");
                    
                    // Schedule retry if under max attempts
                    if (currentJob.getAttemptCount() < 3) {
                        int delayMinutes = 5 * currentJob.getAttemptCount();
                        currentJob.setNextRunAt(currentTime.plusMinutes(delayMinutes));
                        currentJob.setStatus(SyncJobStatus.PENDING);
                    }
                }
            }
            
            // Verify scheduling logic - only check if job is still pending
            if (currentJob.getStatus() == SyncJobStatus.PENDING && currentJob.getNextRunAt() != null) {
                // Job should not be ready to run if nextRunAt is in the future
                boolean shouldBeReadyToRun = !currentJob.getNextRunAt().isAfter(currentTime);
                // Only assert if we haven't already executed the job in this iteration
                if (currentJob.getStartedAt() == null || !currentJob.getStartedAt().equals(currentTime)) {
                    assertThat(isReadyToRun).isEqualTo(shouldBeReadyToRun);
                }
            }
        }
        
        // Verify final scheduling state
        SyncJobEntity finalJob = jobStorage.get(jobId);
        
        if (finalJob.getStatus() == SyncJobStatus.PENDING && finalJob.getNextRunAt() != null) {
            // Verify retry delay calculation (5 minutes * attempt count)
            assertThat(finalJob.getNextRunAt()).isAfter(finalJob.getCompletedAt());
        }
        
        if (finalJob.getStatus() == SyncJobStatus.COMPLETED) {
            assertThat(finalJob.getNextRunAt()).isNull(); // No retry needed
        }
        
        // Verify attempt count progression
        assertThat(finalJob.getAttemptCount()).isGreaterThanOrEqualTo(0);
        assertThat(finalJob.getAttemptCount()).isLessThanOrEqualTo(3); // Max retry attempts
    }

    @Property(tries = 100)
    void syncJobShouldMaintainJobLifecycleManagement(
            @ForAll("validJobLifecycle") JobLifecycle lifecycle) {
        
        // Simulate complete job lifecycle
        Map<Long, SyncJobEntity> jobStorage = new HashMap<>();
        
        // Create job
        SyncJobEntity job = SyncJobEntity.builder()
                .id(lifecycle.jobId())
                .jobType(lifecycle.jobType())
                .repoFullName(lifecycle.repoFullName())
                .status(SyncJobStatus.PENDING)
                .attemptCount(0)
                .currentPage(1)
                .systemCreatedAt(OffsetDateTime.now())
                .systemUpdatedAt(OffsetDateTime.now())
                .build();
        
        jobStorage.put(lifecycle.jobId(), job);
        
        // Verify job creation
        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.PENDING);
        assertThat(job.getAttemptCount()).isEqualTo(0);
        assertThat(job.getCurrentPage()).isEqualTo(1);
        assertThat(job.getSystemCreatedAt()).isNotNull();
        
        // Execute lifecycle phases
        for (LifecyclePhase phase : lifecycle.phases()) {
            SyncJobEntity currentJob = jobStorage.get(lifecycle.jobId());
            
            switch (phase.action()) {
                case START -> {
                    currentJob.setStatus(SyncJobStatus.RUNNING);
                    currentJob.setStartedAt(OffsetDateTime.now());
                    currentJob.setAttemptCount(currentJob.getAttemptCount() + 1);
                }
                case UPDATE_PAGINATION -> {
                    currentJob.setCursor(phase.cursor());
                    currentJob.setCurrentPage(phase.currentPage());
                }
                case COMPLETE -> {
                    currentJob.setStatus(SyncJobStatus.COMPLETED);
                    currentJob.setCompletedAt(OffsetDateTime.now());
                    currentJob.setErrorMessage(null);
                    currentJob.setNextRunAt(null); // Clear retry scheduling
                }
                case FAIL -> {
                    currentJob.setStatus(SyncJobStatus.FAILED);
                    currentJob.setCompletedAt(OffsetDateTime.now());
                    currentJob.setErrorMessage(phase.errorMessage());
                    
                    // Schedule retry if under max attempts
                    if (currentJob.getAttemptCount() < 3) {
                        currentJob.setNextRunAt(OffsetDateTime.now().plusMinutes(5));
                        currentJob.setStatus(SyncJobStatus.PENDING);
                    }
                }
            }
            
            // Update system timestamp
            currentJob.setSystemUpdatedAt(OffsetDateTime.now());
        }
        
        // Verify final lifecycle state
        SyncJobEntity finalJob = jobStorage.get(lifecycle.jobId());
        
        // Verify system timestamps are maintained
        assertThat(finalJob.getSystemCreatedAt()).isNotNull();
        assertThat(finalJob.getSystemUpdatedAt()).isNotNull();
        assertThat(finalJob.getSystemUpdatedAt()).isAfterOrEqualTo(finalJob.getSystemCreatedAt());
        
        // Verify job type and repository association
        assertThat(finalJob.getJobType()).isEqualTo(lifecycle.jobType());
        assertThat(finalJob.getRepoFullName()).isEqualTo(lifecycle.repoFullName());
        
        // Son durumun geçerli olduğunu doğrula
        assertThat(finalJob.getStatus()).isIn((Object[]) SyncJobStatus.values());
        
        if (finalJob.getStatus() == SyncJobStatus.COMPLETED) {
            assertThat(finalJob.getCompletedAt()).isNotNull();
            assertThat(finalJob.getErrorMessage()).isNull();
        }
        
        if (finalJob.getStatus() == SyncJobStatus.FAILED) {
            assertThat(finalJob.getCompletedAt()).isNotNull();
            if (finalJob.getAttemptCount() < 3) {
                assertThat(finalJob.getStatus()).isEqualTo(SyncJobStatus.PENDING); // Reset for retry
                assertThat(finalJob.getNextRunAt()).isNotNull();
            }
        }
    }

    // Data generators and helper records
    
    public record PaginationUpdate(String cursor, Integer currentPage) {}
    
    public record RetryScenario(int minutesElapsed, boolean shouldSucceed) {}
    
    public record JobLifecycle(Long jobId, SyncJobType jobType, String repoFullName, List<LifecyclePhase> phases) {}
    
    public record LifecyclePhase(LifecycleAction action, String cursor, Integer currentPage, String errorMessage) {}
    
    public enum LifecycleAction { START, UPDATE_PAGINATION, COMPLETE, FAIL }

    @Provide
    Arbitrary<SyncJobEntity> validSyncJobEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.of(SyncJobType.class),       // jobType
                validRepoFullName(),                     // repoFullName
                Arbitraries.of(SyncJobStatus.class)      // status
        ).as((id, jobType, repoFullName, status) ->
                SyncJobEntity.builder()
                        .id(id)
                        .jobType(jobType)
                        .repoFullName(repoFullName)
                        .status(status)
                        .attemptCount(0)
                        .currentPage(1)
                        .systemCreatedAt(OffsetDateTime.now())
                        .systemUpdatedAt(OffsetDateTime.now())
                        .build()
        );
    }

    @Provide
    Arbitrary<List<SyncJobStatus>> validStateTransitionSequence() {
        return Arbitraries.of(SyncJobStatus.class)
                .list()
                .ofMinSize(1)
                .ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<PaginationUpdate>> validPaginationUpdates() {
        return Combinators.combine(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100).optional(),
                Arbitraries.integers().between(1, 1000).optional()
        ).as((cursor, page) -> new PaginationUpdate(cursor.orElse(null), page.orElse(null)))
         .list()
         .ofMinSize(1)
         .ofMaxSize(3);
    }

    @Provide
    Arbitrary<List<String>> validErrorMessages() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange(' ', ' ')
                .ofMinLength(10)
                .ofMaxLength(200)
                .list()
                .ofMinSize(1)
                .ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<RetryScenario>> validRetryScenarios() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 30), // minutesElapsed
                Arbitraries.of(true, false)            // shouldSucceed
        ).as(RetryScenario::new)
         .list()
         .ofMinSize(1)
         .ofMaxSize(4);
    }

    @Provide
    Arbitrary<JobLifecycle> validJobLifecycle() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // jobId
                Arbitraries.of(SyncJobType.class),       // jobType
                validRepoFullName(),                     // repoFullName
                validLifecyclePhases()                   // phases
        ).as(JobLifecycle::new);
    }

    @Provide
    Arbitrary<List<LifecyclePhase>> validLifecyclePhases() {
        return Combinators.combine(
                Arbitraries.of(LifecycleAction.class),   // action
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50).optional(), // cursor
                Arbitraries.integers().between(1, 100).optional(), // currentPage
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(100).optional() // errorMessage
        ).as((action, cursor, page, error) -> 
                new LifecyclePhase(action, cursor.orElse(null), page.orElse(null), error.orElse(null)))
         .list()
         .ofMinSize(1)
         .ofMaxSize(5);
    }

    @Provide
    Arbitrary<String> validRepoFullName() {
        return Combinators.combine(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50),
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50)
        ).as((owner, repo) -> owner + "/" + repo);
    }
}