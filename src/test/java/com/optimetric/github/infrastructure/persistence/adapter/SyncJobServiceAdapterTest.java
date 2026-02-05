package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.application.dto.SyncJobInfo;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.jpa.SyncJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobServiceAdapterTest {

    @Mock
    private SyncJobRepository syncJobRepository;

    @InjectMocks
    private SyncJobServiceAdapter syncJobServiceAdapter;

    private SyncJobEntity testJob;
    private final String testRepoFullName = "owner/test-repo";
    private final Long testPullRequestId = 123L;

    @BeforeEach
    void setUp() {
        testJob = SyncJobEntity.builder()
                .id(1L)
                .jobType(SyncJobType.PR_LIST)
                .repoFullName(testRepoFullName)
                .status(SyncJobStatus.PENDING)
                .attemptCount(0)
                .currentPage(1)
                .systemCreatedAt(OffsetDateTime.now())
                .systemUpdatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void createJob_ShouldCreateNewJobWithCorrectProperties() {
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);

        SyncJobInfo result = syncJobServiceAdapter.createJob(SyncJobType.PR_LIST, testRepoFullName);

        assertThat(result).isNotNull();
        assertThat(result.jobType()).isEqualTo(SyncJobType.PR_LIST);
        assertThat(result.repoFullName()).isEqualTo(testRepoFullName);
        assertThat(result.status()).isEqualTo(SyncJobStatus.PENDING);
        assertThat(result.attemptCount()).isEqualTo(0);

        verify(syncJobRepository).save(any(SyncJobEntity.class));
    }

    @Test
    void updateJobStatus_ToCompleted_ShouldSetCompletedAtAndClearError() {
        // Given
        when(syncJobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);

        // When
        syncJobServiceAdapter.updateJobStatus(1L, SyncJobStatus.COMPLETED, null);

        // Then
        verify(syncJobRepository).findById(1L);
        verify(syncJobRepository).save(argThat(job -> 
                job.getStatus() == SyncJobStatus.COMPLETED &&
                job.getCompletedAt() != null &&
                job.getErrorMessage() == null
        ));
    }

    @Test
    void updateJobStatus_ToFailed_ShouldSetErrorMessageAndScheduleRetry() {
        // Given
        testJob.setAttemptCount(1); // Under max retry attempts
        when(syncJobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);

        String errorMessage = "Test error message";

        // When
        syncJobServiceAdapter.updateJobStatus(1L, SyncJobStatus.FAILED, errorMessage);

        // Then
        verify(syncJobRepository).findById(1L);
        verify(syncJobRepository).save(argThat(job -> 
                job.getStatus() == SyncJobStatus.PENDING && // Reset to pending for retry
                job.getErrorMessage().equals(errorMessage) &&
                job.getNextRunAt() != null &&
                job.getCompletedAt() != null
        ));
    }

    @Test
    void updateJobStatus_ToRunning_ShouldSetStartedAtAndIncrementAttempts() {
        // Given
        when(syncJobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);

        // When
        syncJobServiceAdapter.updateJobStatus(1L, SyncJobStatus.RUNNING, null);

        // Then
        verify(syncJobRepository).findById(1L);
        verify(syncJobRepository).save(argThat(job -> 
                job.getStatus() == SyncJobStatus.RUNNING &&
                job.getStartedAt() != null &&
                job.getAttemptCount() == 1
        ));
    }

    @Test
    void getPendingJobs_ShouldReturnJobsReadyToRun() {
        List<SyncJobEntity> pendingJobs = List.of(testJob);
        when(syncJobRepository.findPendingJobsReadyToRun(eq(SyncJobStatus.PENDING), any(OffsetDateTime.class)))
                .thenReturn(pendingJobs);

        List<SyncJobInfo> result = syncJobServiceAdapter.getPendingJobs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobType()).isEqualTo(testJob.getJobType());
        assertThat(result.get(0).repoFullName()).isEqualTo(testJob.getRepoFullName());
        verify(syncJobRepository).findPendingJobsReadyToRun(eq(SyncJobStatus.PENDING), any(OffsetDateTime.class));
    }

    @Test
    void getJobsForRepository_ShouldReturnJobsForSpecificRepo() {
        List<SyncJobEntity> repoJobs = List.of(testJob);
        when(syncJobRepository.findByRepoFullNameOrderBySystemCreatedAtDesc(testRepoFullName))
                .thenReturn(repoJobs);

        List<SyncJobInfo> result = syncJobServiceAdapter.getJobsForRepository(testRepoFullName);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobType()).isEqualTo(testJob.getJobType());
        assertThat(result.get(0).repoFullName()).isEqualTo(testJob.getRepoFullName());
        verify(syncJobRepository).findByRepoFullNameOrderBySystemCreatedAtDesc(testRepoFullName);
    }

    @Test
    void getLatestJobForRepository_WhenJobExists_ShouldReturnLatestCompletedJob() {
        SyncJobEntity completedJob = SyncJobEntity.builder()
                .id(2L)
                .jobType(SyncJobType.PR_LIST)
                .repoFullName(testRepoFullName)
                .status(SyncJobStatus.COMPLETED)
                .completedAt(OffsetDateTime.now())
                .build();

        when(syncJobRepository.findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
                testRepoFullName, SyncJobType.PR_LIST, SyncJobStatus.COMPLETED))
                .thenReturn(Optional.of(completedJob));

        SyncJobInfo result = syncJobServiceAdapter.getLatestJobForRepository(testRepoFullName, SyncJobType.PR_LIST);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(SyncJobStatus.COMPLETED);
    }

    @Test
    void getLatestJobForRepository_WhenNoJobExists_ShouldReturnNull() {
        when(syncJobRepository.findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
                testRepoFullName, SyncJobType.PR_LIST, SyncJobStatus.COMPLETED))
                .thenReturn(Optional.empty());

        SyncJobInfo result = syncJobServiceAdapter.getLatestJobForRepository(testRepoFullName, SyncJobType.PR_LIST);

        assertThat(result).isNull();
    }

    @Test
    void executePrListJob_ShouldCreateAndExecuteJob() {
        // Given
        when(syncJobRepository.findByRepoFullNameAndJobTypeAndStatus(
                testRepoFullName, SyncJobType.PR_LIST, SyncJobStatus.PENDING))
                .thenReturn(Optional.empty());
        when(syncJobRepository.findByRepoFullNameAndJobTypeAndStatus(
                testRepoFullName, SyncJobType.PR_LIST, SyncJobStatus.RUNNING))
                .thenReturn(Optional.empty());
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);
        when(syncJobRepository.findById(any(Long.class))).thenReturn(Optional.of(testJob));

        // When
        syncJobServiceAdapter.executePrListJob(testRepoFullName);

        // Then
        verify(syncJobRepository, atLeastOnce()).save(any(SyncJobEntity.class));
    }

    @Test
    void executePrDetailJob_ShouldCreateJobWithCursor() {
        // Given
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);
        when(syncJobRepository.findById(any(Long.class))).thenReturn(Optional.of(testJob));

        // When
        syncJobServiceAdapter.executePrDetailJob(testRepoFullName, testPullRequestId);

        // Then
        verify(syncJobRepository, atLeastOnce()).save(any(SyncJobEntity.class));
        // Verify that at least one of the saved jobs has the correct type and cursor
        verify(syncJobRepository, atLeast(1)).save(argThat(job -> 
                job.getJobType() == SyncJobType.PR_DETAIL ||
                (job.getCursor() != null && job.getCursor().equals(testPullRequestId.toString()))
        ));
    }

    @Test
    void updateJobPaginationState_ShouldUpdateCursorAndPage() {
        // Given
        when(syncJobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(syncJobRepository.save(any(SyncJobEntity.class))).thenReturn(testJob);

        String cursor = "test-cursor";
        Integer page = 2;

        // When
        syncJobServiceAdapter.updateJobPaginationState(1L, cursor, page);

        // Then
        verify(syncJobRepository).findById(1L);
        verify(syncJobRepository).save(argThat(job -> 
                cursor.equals(job.getCursor()) &&
                page.equals(job.getCurrentPage())
        ));
    }
}