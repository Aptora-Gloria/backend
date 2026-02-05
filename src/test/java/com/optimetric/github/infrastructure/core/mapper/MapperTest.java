package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.api.mapper.PullRequestApiMapper;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.infrastructure.core.mapper.GitHubResponseMapper;
import com.optimetric.github.infrastructure.core.mapper.PullRequestFromApiMapper;
import com.optimetric.github.infrastructure.core.mapper.PullRequestMapper;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void testPullRequestMapper() {
        // Create test repository
        RepositoryEntity repository = new RepositoryEntity();
        repository.setFullName("owner/repo");

        // Create test PR entity
        PullRequestEntity entity = new PullRequestEntity();
        entity.setId(1L);
        entity.setGithubPrId(123L);
        entity.setPrNumber(456);
        entity.setRepository(repository);
        entity.setDataLevel(DataLevel.BASIC);
        entity.setState("open");
        entity.setTitle("Test PR");
        entity.setAuthorLogin("testuser");
        entity.setCreatedAt(OffsetDateTime.now());

        PullRequestDto dto = PullRequestApiMapper.toPullRequestDto(PullRequestMapper.toPullRequestInfo(entity));

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getGithubPrId(), dto.githubPrId());
        assertEquals(entity.getPrNumber(), dto.prNumber());
        assertEquals("owner/repo", dto.repoFullName());
        assertEquals(DataLevel.BASIC, dto.dataLevel());
        assertEquals("open", dto.state());
        assertEquals("Test PR", dto.title());
        assertEquals("testuser", dto.authorLogin());
    }

    @Test
    void testPullRequestMapperWithNullEntity() {
        assertThrows(IllegalArgumentException.class, () -> {
            PullRequestMapper.toPullRequestInfo(null);
        });
    }

    @Test
    void testSyncJobMapper() {
        SyncJobEntity entity = new SyncJobEntity();
        entity.setJobType(SyncJobType.PR_LIST);
        entity.setStatus(SyncJobStatus.COMPLETED);
        entity.setAttemptCount(1);
        entity.setStartedAt(OffsetDateTime.now().minusMinutes(5));
        entity.setCompletedAt(OffsetDateTime.now());

        SyncStatusInfo.JobStatusInfo info = SyncJobMapper.toJobStatusInfo(entity);

        assertNotNull(info);
        assertEquals(SyncJobType.PR_LIST, info.jobType());
        assertEquals(SyncJobStatus.COMPLETED, info.status());
        assertEquals(1, info.attemptCount());
        assertNotNull(info.startedAt());
        assertNotNull(info.completedAt());
    }

    @Test
    void testSyncJobMapperOverallStatus() {
        SyncJobEntity runningJob = new SyncJobEntity();
        runningJob.setStatus(SyncJobStatus.RUNNING);

        SyncJobEntity completedJob = new SyncJobEntity();
        completedJob.setStatus(SyncJobStatus.COMPLETED);

        SyncJobStatus overallStatus = SyncJobMapper.determineOverallSyncStatus(runningJob, completedJob);
        assertEquals(SyncJobStatus.RUNNING, overallStatus);

        overallStatus = SyncJobMapper.determineOverallSyncStatus(completedJob);
        assertEquals(SyncJobStatus.COMPLETED, overallStatus);

        SyncJobEntity failedJob = new SyncJobEntity();
        failedJob.setStatus(SyncJobStatus.FAILED);

        overallStatus = SyncJobMapper.determineOverallSyncStatus(completedJob, failedJob);
        assertEquals(SyncJobStatus.FAILED, overallStatus);
    }

    @Test
    void testGitHubResponseMapper() {
        // Create test repository
        RepositoryEntity repository = new RepositoryEntity();
        repository.setFullName("owner/repo");

        // Create test GitHub response
        GitHubPullRequestResponse.GitHubUserInfo userInfo = 
            new GitHubPullRequestResponse.GitHubUserInfo(789L, "testuser");

        GitHubPullRequestResponse response = new GitHubPullRequestResponse(
            123L, 456, "open", false, "Test PR", "Test body", 
            "https://github.com/owner/repo/pull/456", null,
            OffsetDateTime.now().minusDays(1), OffsetDateTime.now(),
            null, null, userInfo
        );

        PullRequestEntity entity = GitHubResponseMapper.toPullRequestEntity(
                PullRequestFromApiMapper.toPullRequestFromApi(response), repository, null);

        assertNotNull(entity);
        assertEquals(123L, entity.getGithubPrId());
        assertEquals(456, entity.getPrNumber());
        assertEquals(repository, entity.getRepository());
        assertEquals("open", entity.getState());
        assertEquals("Test PR", entity.getTitle());
        assertEquals("testuser", entity.getAuthorLogin());
        assertEquals(789L, entity.getAuthorGithubId());
        assertEquals(DataLevel.BASIC, entity.getDataLevel());
        assertNotNull(entity.getLastSyncedAt());
    }

    @Test
    void testGitHubResponseMapperWithNullInputs() {
        RepositoryEntity repository = new RepositoryEntity();

        assertThrows(IllegalArgumentException.class, () -> {
            GitHubResponseMapper.toPullRequestEntity(null, repository, null);
        });

        GitHubPullRequestResponse response = new GitHubPullRequestResponse(
            123L, 456, "open", false, "Test PR", "Test body", 
            "https://github.com/owner/repo/pull/456", null,
            OffsetDateTime.now(), OffsetDateTime.now(),
            null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> {
            GitHubResponseMapper.toPullRequestEntity(PullRequestFromApiMapper.toPullRequestFromApi(response), null, null);
        });
    }

    @Test
    void testSyncJobMapperWithEmptyList() {
        SyncStatusInfo emptyFromNull = SyncJobMapper.toSyncStatusInfoFromJobInfos("owner/repo", SyncJobStatus.PENDING, null, null);
        assertNotNull(emptyFromNull.jobs());
        assertTrue(emptyFromNull.jobs().isEmpty());

        SyncStatusInfo emptyFromList = SyncJobMapper.toSyncStatusInfoFromJobInfos("owner/repo", SyncJobStatus.PENDING, List.of(), null);
        assertNotNull(emptyFromList.jobs());
        assertTrue(emptyFromList.jobs().isEmpty());
    }

    @Test
    void testSyncJobMapperLastSyncTime() {
        OffsetDateTime time1 = OffsetDateTime.now().minusHours(2);
        OffsetDateTime time2 = OffsetDateTime.now().minusHours(1);

        SyncJobEntity job1 = new SyncJobEntity();
        job1.setCompletedAt(time1);

        SyncJobEntity job2 = new SyncJobEntity();
        job2.setCompletedAt(time2);

        SyncJobEntity job3 = new SyncJobEntity();
        // No completion time

        OffsetDateTime lastTime = SyncJobMapper.getLastSyncTime(job1, job2, job3);
        assertEquals(time2, lastTime); // Should return the most recent time

        // Test with no completed jobs
        lastTime = SyncJobMapper.getLastSyncTime(job3);
        assertNull(lastTime);
    }
}