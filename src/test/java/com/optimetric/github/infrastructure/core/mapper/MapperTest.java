package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.github.infrastructure.core.mapper.GitHubResponseMapper;
import com.optimetric.github.infrastructure.core.mapper.PullRequestMapper;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for mapper classes to ensure proper conversion between entities and DTOs.
 */
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

        // Test mapping to DTO
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getGithubPrId(), dto.githubPrId());
        assertEquals(entity.getPrNumber(), dto.prNumber());
        assertEquals("owner/repo", dto.repoFullName());
        assertEquals(com.optimetric.github.domain.enumeration.DataLevel.BASIC, dto.dataLevel());
        assertEquals("open", dto.state());
        assertEquals("Test PR", dto.title());
        assertEquals("testuser", dto.authorLogin());
    }

    @Test
    void testPullRequestMapperWithNullEntity() {
        assertThrows(IllegalArgumentException.class, () -> {
            PullRequestMapper.toDto(null);
        });
    }

    @Test
    void testSyncJobMapper() {
        // Create test sync job entity (entity uses persistence enums)
        SyncJobEntity entity = new SyncJobEntity();
        entity.setJobType(com.optimetric.github.infrastructure.persistence.entity.SyncJobType.PR_LIST);
        entity.setStatus(com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus.COMPLETED);
        entity.setAttemptCount(1);
        entity.setStartedAt(OffsetDateTime.now().minusMinutes(5));
        entity.setCompletedAt(OffsetDateTime.now());

        // Test mapping to DTO (DTO uses domain enums)
        SyncStatusDto.JobStatusDto dto = SyncJobMapper.toJobStatusDto(entity);

        assertNotNull(dto);
        assertEquals(com.optimetric.github.domain.enumeration.SyncJobType.PR_LIST, dto.jobType());
        assertEquals(SyncJobStatus.COMPLETED, dto.status());
        assertEquals(1, dto.attemptCount());
        assertNotNull(dto.startedAt());
        assertNotNull(dto.completedAt());
    }

    @Test
    void testSyncJobMapperOverallStatus() {
        // Test with running job (entity uses persistence enum)
        SyncJobEntity runningJob = new SyncJobEntity();
        runningJob.setStatus(com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus.RUNNING);

        SyncJobEntity completedJob = new SyncJobEntity();
        completedJob.setStatus(com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus.COMPLETED);

        SyncJobStatus overallStatus = SyncJobMapper.determineOverallSyncStatus(runningJob, completedJob);
        assertEquals(SyncJobStatus.RUNNING, overallStatus);

        // Test with only completed jobs
        overallStatus = SyncJobMapper.determineOverallSyncStatus(completedJob);
        assertEquals(SyncJobStatus.COMPLETED, overallStatus);

        // Test with failed job
        SyncJobEntity failedJob = new SyncJobEntity();
        failedJob.setStatus(com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus.FAILED);

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

        // Test mapping to entity
        PullRequestEntity entity = GitHubResponseMapper.toPullRequestEntity(response, repository, null);

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
            GitHubResponseMapper.toPullRequestEntity(response, null, null);
        });
    }

    @Test
    void testSyncJobMapperWithEmptyList() {
        List<SyncStatusDto.JobStatusDto> dtos = SyncJobMapper.toJobStatusDtos(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());

        dtos = SyncJobMapper.toJobStatusDtos(List.of());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
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