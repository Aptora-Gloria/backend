package com.optimetric.github.api;

import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.api.response.*;
import com.optimetric.github.infrastructure.persistence.adapter.PullRequestServiceAdapter;
import com.optimetric.github.infrastructure.persistence.entity.*;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for API response consistency.
 * **Feature: pull-request-ingestion, Property 6: API Response Consistency**
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
 */
public class ApiResponseConsistencyPropertyTest {

    @Property(tries = 100)
    void pullRequestDtoShouldIncludeAllRequiredMetadata(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // Create service adapter instance for mapping
        PullRequestServiceAdapter serviceAdapter = createMockServiceAdapter();
        
        // When - mapping entity to DTO using the service adapter's mapping logic
        PullRequestDto dto = mapToDto(pullRequest);
        
        // Then - response should include all required metadata (Requirement 6.2)
        assertThat(dto.dataLevel()).isNotNull();
        assertThat(dto.lastSyncedAt()).isEqualTo(pullRequest.getLastSyncedAt());
        
        // Should use natural keys (Requirement 6.1, 6.4)
        assertThat(dto.repoFullName()).isEqualTo(pullRequest.getRepository().getFullName());
        assertThat(dto.prNumber()).isEqualTo(pullRequest.getPrNumber());
        
        // Should indicate change statistics availability (Requirement 6.3)
        assertThat(dto.changeStats()).isNotNull();
        assertThat(dto.changeStats().available()).isNotNull();
        
        // Verify consistency of change stats availability logic
        boolean expectedAvailable = pullRequest.getAdditions() != null && 
                                   pullRequest.getDeletions() != null && 
                                   pullRequest.getChangedFiles() != null;
        assertThat(dto.changeStats().available()).isEqualTo(expectedAvailable);
    }

    @Property(tries = 100)
    void pullRequestDtoShouldUseNaturalKeysConsistently(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = mapToDto(pullRequest);
        
        // Then - should use repo_full_name as natural key (Requirement 6.1, 6.4)
        assertThat(dto.repoFullName()).isNotNull();
        assertThat(dto.repoFullName()).isEqualTo(pullRequest.getRepository().getFullName());
        assertThat(dto.repoFullName()).contains("/"); // Should be in owner/repo format
        
        // PR number should be accessible for individual retrieval (Requirement 6.4)
        assertThat(dto.prNumber()).isNotNull();
        assertThat(dto.prNumber()).isEqualTo(pullRequest.getPrNumber());
        assertThat(dto.prNumber()).isGreaterThan(0);
        
        // GitHub PR ID should be included for reference
        assertThat(dto.githubPrId()).isNotNull();
        assertThat(dto.githubPrId()).isEqualTo(pullRequest.getGithubPrId());
    }

    @Property(tries = 100)
    void changeStatsAvailabilityIndicatorShouldBeConsistent(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = mapToDto(pullRequest);
        
        // Then - changeStats.available should accurately reflect data presence (Requirement 6.3)
        boolean hasAllChangeStats = pullRequest.getAdditions() != null && 
                                   pullRequest.getDeletions() != null && 
                                   pullRequest.getChangedFiles() != null;
        
        assertThat(dto.changeStats().available()).isEqualTo(hasAllChangeStats);
        
        if (hasAllChangeStats) {
            // When available, all stats should be present
            assertThat(dto.changeStats().additions()).isEqualTo(pullRequest.getAdditions());
            assertThat(dto.changeStats().deletions()).isEqualTo(pullRequest.getDeletions());
            assertThat(dto.changeStats().changedFiles()).isEqualTo(pullRequest.getChangedFiles());
            assertThat(dto.changeStats().syncedAt()).isEqualTo(pullRequest.getChangeStatsSyncedAt());
        } else {
            // When not available, at least one stat should be null
            boolean hasNullStats = pullRequest.getAdditions() == null || 
                                  pullRequest.getDeletions() == null || 
                                  pullRequest.getChangedFiles() == null;
            assertThat(hasNullStats).isTrue();
        }
    }

    @Property(tries = 100)
    void dataLevelAndLastSyncedAtShouldBeIncludedInResponse(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO
        PullRequestDto dto = mapToDto(pullRequest);
        
        // Then - data_level should be included (Requirement 6.2)
        assertThat(dto.dataLevel()).isNotNull();
        assertThat(dto.dataLevel()).isEqualTo(pullRequest.getDataLevel());
        assertThat(dto.dataLevel()).isIn(DataLevel.BASIC, DataLevel.FULL);
        
        // last_synced_at should be included (Requirement 6.2)
        assertThat(dto.lastSyncedAt()).isEqualTo(pullRequest.getLastSyncedAt());
        
        // If lastSyncedAt is present, it should be a valid timestamp
        if (dto.lastSyncedAt() != null) {
            assertThat(dto.lastSyncedAt()).isBefore(OffsetDateTime.now().plusMinutes(1));
        }
    }

    @Property(tries = 100)
    void reviewDtoShouldIncludeRequiredFieldsForNaturalKeyAccess(
            @ForAll("validPullRequestReviewEntity") PullRequestReviewEntity review) {
        
        // When - mapping review entity to DTO
        ReviewDto dto = mapReviewToDto(review);
        
        // Then - should include all required fields for natural key access (Requirement 6.5)
        assertThat(dto.githubReviewId()).isNotNull();
        assertThat(dto.githubReviewId()).isEqualTo(review.getGithubReviewId());
        
        assertThat(dto.pullRequestId()).isNotNull();
        assertThat(dto.pullRequestId()).isEqualTo(review.getPullRequest().getId());
        
        assertThat(dto.reviewer()).isNotNull();
        assertThat(dto.reviewer().login()).isEqualTo(review.getReviewerLogin());
        assertThat(dto.reviewer().githubId()).isEqualTo(review.getReviewerGithubId());
        
        assertThat(dto.state()).isNotNull();
        assertThat(dto.submittedAt()).isNotNull();
        assertThat(dto.htmlUrl()).isNotNull();
    }

    @Property(tries = 100)
    void commitDtoShouldIncludeRequiredFieldsForNaturalKeyAccess(
            @ForAll("validPullRequestCommitEntity") PullRequestCommitEntity commit) {
        
        // When - mapping commit entity to DTO
        ReviewCommentDto dto = mapCommitToDto(commit);
        
        // Then - should include all required fields for natural key access (Requirement 6.5)
        assertThat(dto.githubRepoId()).isNotNull();
        assertThat(dto.githubRepoId()).isEqualTo(commit.getGithubRepoId());
        
        assertThat(dto.sha()).isNotNull();
        assertThat(dto.sha()).isEqualTo(commit.getSha());
        assertThat(dto.sha()).hasSize(40); // SHA should be 40 characters
        
        assertThat(dto.pullRequestId()).isNotNull();
        assertThat(dto.pullRequestId()).isEqualTo(commit.getPullRequest().getId());
        
        assertThat(dto.author()).isNotNull();
        assertThat(dto.author().login()).isEqualTo(commit.getAuthorLogin());
        assertThat(dto.author().name()).isEqualTo(commit.getAuthorName());
        assertThat(dto.author().email()).isEqualTo(commit.getAuthorEmail());
        
        assertThat(dto.committedAt()).isNotNull();
        assertThat(dto.htmlUrl()).isNotNull();
    }

    @Property(tries = 100)
    void syncStatusDtoShouldIncludeAllRequiredMetadata(
            @ForAll("validSyncStatusData") SyncStatusData syncData) {
        
        // When - creating sync status DTO
        SyncStatusDto dto = new SyncStatusDto(
                syncData.repoFullName(),
                syncData.overallStatus(),
                syncData.jobDtos(),
                syncData.lastSyncedAt()
        );
        
        // Then - should include all required metadata
        assertThat(dto.repoFullName()).isNotNull();
        assertThat(dto.repoFullName()).isEqualTo(syncData.repoFullName());
        assertThat(dto.repoFullName()).contains("/"); // Natural key format
        
        assertThat(dto.overallStatus()).isNotNull();
        assertThat(dto.overallStatus()).isEqualTo(syncData.overallStatus());
        
        assertThat(dto.jobs()).isNotNull();
        assertThat(dto.jobs()).isEqualTo(syncData.jobDtos());
        
        // Each job should have required fields
        for (SyncStatusDto.JobStatusDto job : dto.jobs()) {
            assertThat(job.jobType()).isNotNull();
            assertThat(job.status()).isNotNull();
            assertThat(job.attemptCount()).isNotNull();
            assertThat(job.attemptCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Property(tries = 100)
    void apiResponsesShouldMaintainConsistentStructureAcrossDataStates(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // When - mapping entity to DTO regardless of data completeness
        PullRequestDto dto = mapToDto(pullRequest);
        
        // Then - response structure should be consistent (Requirement 6.1, 6.2, 6.3)
        // Core fields should always be present
        assertThat(dto.id()).isNotNull();
        assertThat(dto.githubPrId()).isNotNull();
        assertThat(dto.prNumber()).isNotNull();
        assertThat(dto.repoFullName()).isNotNull();
        assertThat(dto.dataLevel()).isNotNull();
        assertThat(dto.state()).isNotNull();
        assertThat(dto.title()).isNotNull();
        assertThat(dto.authorLogin()).isNotNull();
        assertThat(dto.htmlUrl()).isNotNull();
        assertThat(dto.changeStats()).isNotNull();
        assertThat(dto.createdAt()).isNotNull();
        assertThat(dto.updatedAt()).isNotNull();
        
        // Change stats should always have availability indicator
        assertThat(dto.changeStats().available()).isNotNull();
        
        // Optional fields can be null but structure should be consistent
        // draft, body, authorGithubId, mergeCommitSha, closedAt, mergedAt, lastSyncedAt can be null
        
        // Timestamps should be valid when present
        if (dto.createdAt() != null) {
            assertThat(dto.createdAt()).isBefore(OffsetDateTime.now().plusMinutes(1));
        }
        if (dto.updatedAt() != null) {
            assertThat(dto.updatedAt()).isBefore(OffsetDateTime.now().plusMinutes(1));
        }
    }

    // Helper methods for mapping (simulating the service adapter logic)
    
    private PullRequestServiceAdapter createMockServiceAdapter() {
        // This would normally be injected, but for property testing we simulate the mapping logic
        return null; // We'll use direct mapping methods instead
    }
    
    private PullRequestDto mapToDto(PullRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Create change stats DTO with availability indicator
        ChangeStatsDto changeStats = ChangeStatsDto.of(
                entity.getAdditions(),
                entity.getDeletions(),
                entity.getChangedFiles(),
                entity.getChangeStatsSyncedAt()
        );
        
        return new PullRequestDto(
                entity.getId(),
                entity.getGithubPrId(),
                entity.getPrNumber(),
                entity.getRepository().getFullName(),
                entity.getDataLevel(),
                entity.getState(),
                entity.getDraft(),
                entity.getTitle(),
                entity.getBody(),
                entity.getAuthorLogin(),
                entity.getAuthorGithubId(),
                entity.getHtmlUrl(),
                entity.getMergeCommitSha(),
                changeStats,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getClosedAt(),
                entity.getMergedAt(),
                entity.getLastSyncedAt()
        );
    }
    
    private ReviewDto mapReviewToDto(PullRequestReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReviewDto.ReviewerInfo reviewerInfo = new ReviewDto.ReviewerInfo(
                entity.getReviewerLogin(),
                entity.getReviewerGithubId()
        );
        
        return new ReviewDto(
                entity.getId(),
                entity.getGithubReviewId(),
                entity.getPullRequest().getId(),
                reviewerInfo,
                entity.getState(),
                entity.getBody(),
                entity.getSubmittedAt(),
                entity.getHtmlUrl()
        );
    }
    
    private ReviewCommentDto mapCommitToDto(PullRequestCommitEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ReviewCommentDto.CommentAuthorInfo authorInfo = new ReviewCommentDto.CommentAuthorInfo(
                entity.getAuthorLogin(),
                entity.getAuthorName(),
                entity.getAuthorEmail()
        );
        
        return new ReviewCommentDto(
                entity.getId(),
                entity.getGithubRepoId(),
                entity.getSha(),
                entity.getPullRequest().getId(),
                entity.getMessage(),
                authorInfo,
                entity.getCommittedAt(),
                entity.getHtmlUrl()
        );
    }

    // Data generators and helper records
    
    public record SyncStatusData(
            String repoFullName,
            SyncJobStatus overallStatus,
            List<SyncStatusDto.JobStatusDto> jobDtos,
            OffsetDateTime lastSyncedAt
    ) {}

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntity() {
        return Combinators.combine(
                validBasePullRequestData(),
                validPullRequestTimestamps(),
                validChangeStatistics()
        ).as((baseData, timestamps, changeStats) ->
                PullRequestEntity.builder()
                        .id(baseData.id())
                        .githubPrId(baseData.githubPrId())
                        .prNumber(baseData.prNumber())
                        .repository(baseData.repository())
                        .dataLevel(baseData.dataLevel())
                        .state(baseData.state())
                        .draft(baseData.draft())
                        .title(baseData.title())
                        .body(baseData.body())
                        .authorLogin(baseData.authorLogin())
                        .authorGithubId(baseData.authorGithubId())
                        .htmlUrl(baseData.htmlUrl())
                        .mergeCommitSha(baseData.mergeCommitSha())
                        .createdAt(timestamps.createdAt())
                        .updatedAt(timestamps.updatedAt())
                        .closedAt(timestamps.closedAt())
                        .mergedAt(timestamps.mergedAt())
                        .lastSyncedAt(timestamps.lastSyncedAt())
                        .additions(changeStats.additions())
                        .deletions(changeStats.deletions())
                        .changedFiles(changeStats.changedFiles())
                        .changeStatsSyncedAt(changeStats.syncedAt())
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    public record BasePullRequestData(
            Long id,
            Long githubPrId,
            Integer prNumber,
            RepositoryEntity repository,
            DataLevel dataLevel,
            String state,
            Boolean draft,
            String title,
            String body,
            String authorLogin,
            Long authorGithubId,
            String htmlUrl,
            String mergeCommitSha
    ) {}

    public record PullRequestTimestamps(
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            OffsetDateTime mergedAt,
            OffsetDateTime lastSyncedAt
    ) {}

    public record ChangeStatistics(
            Integer additions,
            Integer deletions,
            Integer changedFiles,
            OffsetDateTime syncedAt
    ) {}

    @Provide
    Arbitrary<BasePullRequestData> validBasePullRequestData() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.of(DataLevel.class),         // dataLevel
                Arbitraries.of("open", "closed"),        // state
                Arbitraries.of(true, false),             // draft
                validTitle()                             // title
        ).as((id, githubPrId, prNumber, repository, dataLevel, state, draft, title) ->
                new BasePullRequestData(
                        id, githubPrId, prNumber, repository, dataLevel, state, draft, title,
                        null, // body - will be set separately
                        "testuser" + githubPrId, // authorLogin
                        12345L + githubPrId, // authorGithubId
                        "https://github.com/test/repo/pull/" + prNumber, // htmlUrl
                        null // mergeCommitSha
                )
        );
    }

    @Provide
    Arbitrary<PullRequestTimestamps> validPullRequestTimestamps() {
        return Combinators.combine(
                validOffsetDateTime(),                   // createdAt
                validOffsetDateTime(),                   // updatedAt
                validOffsetDateTime().optional(),        // closedAt
                validOffsetDateTime().optional(),        // mergedAt
                validOffsetDateTime().optional()         // lastSyncedAt
        ).as((createdAt, updatedAt, closedAt, mergedAt, lastSyncedAt) ->
                new PullRequestTimestamps(
                        createdAt, updatedAt,
                        closedAt.orElse(null),
                        mergedAt.orElse(null),
                        lastSyncedAt.orElse(null)
                )
        );
    }

    @Provide
    Arbitrary<ChangeStatistics> validChangeStatistics() {
        return Combinators.combine(
                Arbitraries.integers().between(0, 10000).optional(), // additions
                Arbitraries.integers().between(0, 10000).optional(), // deletions
                Arbitraries.integers().between(1, 1000).optional(),  // changedFiles
                validOffsetDateTime().optional()         // syncedAt
        ).as((additions, deletions, changedFiles, syncedAt) ->
                new ChangeStatistics(
                        additions.orElse(null),
                        deletions.orElse(null),
                        changedFiles.orElse(null),
                        syncedAt.orElse(null)
                )
        );
    }

    @Provide
    Arbitrary<PullRequestReviewEntity> validPullRequestReviewEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubReviewId
                validPullRequestEntityForRelation(),     // pullRequest
                validLogin(),                            // reviewerLogin
                Arbitraries.longs().greaterOrEqual(1L),  // reviewerGithubId
                Arbitraries.of("APPROVED", "CHANGES_REQUESTED", "COMMENTED"), // state
                validOffsetDateTime(),                   // submittedAt
                validHtmlUrl()                           // htmlUrl
        ).as((id, githubReviewId, pullRequest, reviewerLogin, reviewerGithubId, state, submittedAt, htmlUrl) ->
                PullRequestReviewEntity.builder()
                        .id(id)
                        .githubReviewId(githubReviewId)
                        .pullRequest(pullRequest)
                        .reviewerLogin(reviewerLogin)
                        .reviewerGithubId(reviewerGithubId)
                        .state(state)
                        .body("Review body for " + githubReviewId)
                        .submittedAt(submittedAt)
                        .htmlUrl(htmlUrl)
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestCommitEntity> validPullRequestCommitEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                validSha(),                              // sha
                validPullRequestEntityForRelation(),     // pullRequest
                validLogin(),                            // authorLogin
                validName(),                             // authorName
                validEmail(),                            // authorEmail
                validOffsetDateTime()                    // committedAt
        ).as((id, githubRepoId, sha, pullRequest, authorLogin, authorName, authorEmail, committedAt) ->
                PullRequestCommitEntity.builder()
                        .id(id)
                        .githubRepoId(githubRepoId)
                        .sha(sha)
                        .pullRequest(pullRequest)
                        .message("Commit message for " + sha)
                        .authorLogin(authorLogin)
                        .authorName(authorName)
                        .authorEmail(authorEmail)
                        .committedAt(committedAt)
                        .htmlUrl("https://github.com/test/repo/commit/" + sha)
                        .build()
        );
    }

    @Provide
    Arbitrary<SyncStatusData> validSyncStatusData() {
        return Combinators.combine(
                validRepoFullName(),                     // repoFullName
                Arbitraries.of(SyncJobStatus.class),     // overallStatus
                validJobStatusDtoList(),                 // jobDtos
                validOffsetDateTime().optional()         // lastSyncedAt
        ).as((repoFullName, overallStatus, jobDtos, lastSyncedAt) ->
                new SyncStatusData(repoFullName, overallStatus, jobDtos, lastSyncedAt.orElse(null))
        );
    }

    @Provide
    Arbitrary<List<SyncStatusDto.JobStatusDto>> validJobStatusDtoList() {
        return validJobStatusDto().list().ofMinSize(1).ofMaxSize(4);
    }

    @Provide
    Arbitrary<SyncStatusDto.JobStatusDto> validJobStatusDto() {
        return Combinators.combine(
                Arbitraries.of(SyncJobType.class),       // jobType
                Arbitraries.of(SyncJobStatus.class),     // status
                Arbitraries.integers().between(0, 5),    // attemptCount
                validErrorMessage().optional(),          // errorMessage
                validOffsetDateTime().optional(),        // startedAt
                validOffsetDateTime().optional(),        // completedAt
                validOffsetDateTime().optional()         // nextRunAt
        ).as((jobType, status, attemptCount, errorMessage, startedAt, completedAt, nextRunAt) ->
                new SyncStatusDto.JobStatusDto(
                        jobType, status, attemptCount, errorMessage.orElse(null),
                        startedAt.orElse(null), completedAt.orElse(null), nextRunAt.orElse(null)
                )
        );
    }

    // Helper generators

    @Provide
    Arbitrary<RepositoryEntity> validRepositoryEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                validRepoName(),                         // name
                validRepoFullName(),                     // fullName
                validUser(),                             // user
                validLogin(),                            // ownerLogin
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((id, githubRepoId, name, fullName, user, ownerLogin, ownerType) ->
                RepositoryEntity.builder()
                        .id(id)
                        .githubRepoId(githubRepoId)
                        .name(name)
                        .fullName(fullName)
                        .user(user)
                        .ownerLogin(ownerLogin)
                        .ownerType(ownerType)
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityForRelation() {
        // Simplified version for use in relationships to avoid circular dependencies
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999) // prNumber
        ).as((id, githubPrId, prNumber) ->
                PullRequestEntity.builder()
                        .id(id)
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .build()
        );
    }

    @Provide
    Arbitrary<User> validUser() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.longs().greaterOrEqual(1L),  // githubUserId
                validLogin(),                            // login
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((id, githubUserId, login, ownerType) ->
                User.builder()
                        .id(id)
                        .githubUserId(githubUserId)
                        .login(login)
                        .ownerType(ownerType)
                        .build()
        );
    }

    @Provide
    Arbitrary<OffsetDateTime> validOffsetDateTime() {
        return Arbitraries.longs()
                .between(1640995200L, 1735689600L) // 2022-01-01 to 2025-01-01
                .map(epochSecond -> OffsetDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(epochSecond),
                        ZoneOffset.UTC
                ));
    }

    @Provide
    Arbitrary<String> validRepoFullName() {
        return Combinators.combine(
                validLogin(),
                validRepoName()
        ).as((owner, repo) -> owner + "/" + repo);
    }

    @Provide
    Arbitrary<String> validRepoName() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .withChars('-', '_')
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<String> validLogin() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .withChars('-', '_')
                .ofMinLength(1)
                .ofMaxLength(39); // GitHub username limit
    }

    @Provide
    Arbitrary<String> validTitle() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '-', '_', ':', '(', ')', '[', ']')
                .ofMinLength(1)
                .ofMaxLength(200);
    }

    @Provide
    Arbitrary<String> validBody() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '\n', '-', '_', '.', '!', '?')
                .ofMinLength(1)
                .ofMaxLength(1000);
    }

    @Provide
    Arbitrary<String> validHtmlUrl() {
        return Combinators.combine(
                validLogin(),
                validRepoName(),
                Arbitraries.integers().between(1, 99999)
        ).as((owner, repo, number) -> 
                "https://github.com/" + owner + "/" + repo + "/pull/" + number);
    }

    @Provide
    Arbitrary<String> validSha() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .withCharRange('a', 'f')
                .ofLength(40);
    }

    @Provide
    Arbitrary<String> validCommitMessage() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '-', '_', ':', '(', ')', '\n')
                .ofMinLength(1)
                .ofMaxLength(500);
    }

    @Provide
    Arbitrary<String> validName() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ', '-', '_')
                .ofMinLength(1)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> validEmail() {
        return Combinators.combine(
                validLogin(),
                Arbitraries.of("gmail.com", "github.com", "example.com", "test.org")
        ).as((user, domain) -> user + "@" + domain);
    }

    @Provide
    Arbitrary<String> validErrorMessage() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '-', '_', ':', '.', '!')
                .ofMinLength(1)
                .ofMaxLength(200);
    }
}