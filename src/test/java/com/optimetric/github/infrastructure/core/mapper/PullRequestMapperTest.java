package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.ChangeStatsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.infrastructure.persistence.entity.*;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.shared.domain.OwnerType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for PullRequestMapper.
 * Tests Requirements 7.1, 7.2, and 7.5 implementation.
 */
class PullRequestMapperTest {

    @Test
    void toDto_shouldSetChangeStatsAvailableTrue_whenAllChangeStatsPresent() {
        // Given - PR with all change statistics (Requirement 7.1)
        PullRequestEntity entity = createBasicPullRequestEntity();
        entity.setAdditions(100);
        entity.setDeletions(50);
        entity.setChangedFiles(5);
        entity.setChangeStatsSyncedAt(OffsetDateTime.now());

        // When
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        // Then - changeStats.available should be true
        assertThat(dto.changeStats().available()).isTrue();
        assertThat(dto.changeStats().additions()).isEqualTo(100);
        assertThat(dto.changeStats().deletions()).isEqualTo(50);
        assertThat(dto.changeStats().changedFiles()).isEqualTo(5);
        assertThat(dto.changeStats().syncedAt()).isNotNull();
    }

    @Test
    void toDto_shouldSetChangeStatsAvailableFalse_whenChangeStatsNull() {
        // Given - PR with null change statistics (Requirement 7.2)
        PullRequestEntity entity = createBasicPullRequestEntity();
        entity.setAdditions(null);
        entity.setDeletions(null);
        entity.setChangedFiles(null);
        entity.setChangeStatsSyncedAt(null);

        // When
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        // Then - changeStats.available should be false
        assertThat(dto.changeStats().available()).isFalse();
        assertThat(dto.changeStats().additions()).isNull();
        assertThat(dto.changeStats().deletions()).isNull();
        assertThat(dto.changeStats().changedFiles()).isNull();
        assertThat(dto.changeStats().syncedAt()).isNull();
    }

    @Test
    void toDto_shouldSetChangeStatsAvailableFalse_whenPartialChangeStats() {
        // Given - PR with partial change statistics (Requirement 7.2)
        PullRequestEntity entity = createBasicPullRequestEntity();
        entity.setAdditions(100);
        entity.setDeletions(null); // Missing deletions
        entity.setChangedFiles(5);
        entity.setChangeStatsSyncedAt(OffsetDateTime.now());

        // When
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        // Then - changeStats.available should be false
        assertThat(dto.changeStats().available()).isFalse();
    }

    @Test
    void toDto_shouldIncludeLastSyncedAtTimestamp() {
        // Given - PR with last synced timestamp (Requirement 7.5)
        PullRequestEntity entity = createBasicPullRequestEntity();
        OffsetDateTime lastSynced = OffsetDateTime.now();
        entity.setLastSyncedAt(lastSynced);

        // When
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        // Then - lastSyncedAt should be included
        assertThat(dto.lastSyncedAt()).isEqualTo(lastSynced);
    }

    @Test
    void toDto_shouldMapAllBasicFields() {
        // Given
        PullRequestEntity entity = createBasicPullRequestEntity();

        // When
        PullRequestDto dto = PullRequestMapper.toDto(entity);

        // Then
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.githubPrId()).isEqualTo(entity.getGithubPrId());
        assertThat(dto.prNumber()).isEqualTo(entity.getPrNumber());
        assertThat(dto.repoFullName()).isEqualTo(entity.getRepository().getFullName());
        assertThat(dto.dataLevel()).isEqualTo(entity.getDataLevel());
        assertThat(dto.state()).isEqualTo(entity.getState());
        assertThat(dto.draft()).isEqualTo(entity.getDraft());
        assertThat(dto.title()).isEqualTo(entity.getTitle());
        assertThat(dto.authorLogin()).isEqualTo(entity.getAuthorLogin());
        assertThat(dto.authorGithubId()).isEqualTo(entity.getAuthorGithubId());
    }

    @Test
    void toReviewDto_shouldMapAllFields() {
        // Given
        PullRequestReviewEntity entity = createReviewEntity();

        // When
        ReviewDto dto = PullRequestMapper.toReviewDto(entity);

        // Then
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.githubReviewId()).isEqualTo(entity.getGithubReviewId());
        assertThat(dto.pullRequestId()).isEqualTo(entity.getPullRequest().getId());
        assertThat(dto.reviewer().login()).isEqualTo(entity.getReviewerLogin());
        assertThat(dto.reviewer().githubId()).isEqualTo(entity.getReviewerGithubId());
        assertThat(dto.state()).isEqualTo(entity.getState());
        assertThat(dto.body()).isEqualTo(entity.getBody());
        assertThat(dto.submittedAt()).isEqualTo(entity.getSubmittedAt());
        assertThat(dto.htmlUrl()).isEqualTo(entity.getHtmlUrl());
    }

    @Test
    void toCommitDto_shouldMapAllFields() {
        // Given
        PullRequestCommitEntity entity = createCommitEntity();

        // When
        ReviewCommentDto dto = PullRequestMapper.toCommitDto(entity);

        // Then
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.githubRepoId()).isEqualTo(entity.getGithubRepoId());
        assertThat(dto.sha()).isEqualTo(entity.getSha());
        assertThat(dto.pullRequestId()).isEqualTo(entity.getPullRequest().getId());
        assertThat(dto.message()).isEqualTo(entity.getMessage());
        assertThat(dto.author().login()).isEqualTo(entity.getAuthorLogin());
        assertThat(dto.author().name()).isEqualTo(entity.getAuthorName());
        assertThat(dto.author().email()).isNull(); // Email not stored in entity
        assertThat(dto.committedAt()).isEqualTo(entity.getCommittedAt());
        assertThat(dto.htmlUrl()).isEqualTo(entity.getHtmlUrl());
    }

    @Test
    void toDtos_shouldMapListCorrectly() {
        // Given
        List<PullRequestEntity> entities = List.of(
                createBasicPullRequestEntity(),
                createBasicPullRequestEntity()
        );

        // When
        List<PullRequestDto> dtos = PullRequestMapper.toDtos(entities);

        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).githubPrId()).isEqualTo(entities.get(0).getGithubPrId());
        assertThat(dtos.get(1).githubPrId()).isEqualTo(entities.get(1).getGithubPrId());
    }

    @Test
    void toDto_shouldThrowException_whenEntityIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                PullRequestMapper.toDto(null));
    }

    @Test
    void toReviewDto_shouldThrowException_whenEntityIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                PullRequestMapper.toReviewDto(null));
    }

    @Test
    void toCommitDto_shouldThrowException_whenEntityIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                PullRequestMapper.toCommitDto(null));
    }

    // Helper methods

    private PullRequestEntity createBasicPullRequestEntity() {
        RepositoryEntity repository = RepositoryEntity.builder()
                .id(1L)
                .githubRepoId(12345L)
                .name("test-repo")
                .fullName("owner/test-repo")
                .user(createUser())
                .ownerLogin("owner")
                .ownerType(OwnerType.USER)
                .build();

        return PullRequestEntity.builder()
                .id(1L)
                .githubPrId(67890L)
                .prNumber(123)
                .repository(repository)
                .dataLevel(DataLevel.BASIC)
                .state("open")
                .draft(false)
                .title("Test PR")
                .body("Test PR body")
                .authorLogin("testuser")
                .authorGithubId(11111L)
                .htmlUrl("https://github.com/owner/test-repo/pull/123")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .lastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    private PullRequestReviewEntity createReviewEntity() {
        PullRequestEntity pullRequest = createBasicPullRequestEntity();
        
        return PullRequestReviewEntity.builder()
                .id(1L)
                .githubReviewId(98765L)
                .pullRequest(pullRequest)
                .reviewerLogin("reviewer")
                .reviewerGithubId(22222L)
                .state("APPROVED")
                .body("Looks good!")
                .submittedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .htmlUrl("https://github.com/owner/test-repo/pull/123#review-98765")
                .build();
    }

    private PullRequestCommitEntity createCommitEntity() {
        PullRequestEntity pullRequest = createBasicPullRequestEntity();
        
        return PullRequestCommitEntity.builder()
                .id(1L)
                .githubRepoId(12345L)
                .sha("abc123def456")
                .pullRequest(pullRequest)
                .message("Test commit message")
                .authorLogin("committer")
                .authorName("Committer Name")
                .committedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .htmlUrl("https://github.com/owner/test-repo/commit/abc123def456")
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .githubUserId(33333L)
                .login("testuser")
                .ownerType(OwnerType.USER)
                .build();
    }
}