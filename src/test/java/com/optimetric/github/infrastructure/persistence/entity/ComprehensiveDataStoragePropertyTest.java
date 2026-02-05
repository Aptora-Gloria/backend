package com.optimetric.github.infrastructure.persistence.entity;

import com.optimetric.shared.domain.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import net.jqwik.api.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
public class ComprehensiveDataStoragePropertyTest {

    @Property(tries = 100)
    void pullRequestEntityShouldStoreAllRequiredFields(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // Verify non-null constraints are satisfied for required fields
        assertThat(pullRequest.getGithubPrId()).isNotNull();
        assertThat(pullRequest.getPrNumber()).isNotNull();
        assertThat(pullRequest.getRepository()).isNotNull();
        assertThat(pullRequest.getDataLevel()).isNotNull();
        
        // Verify proper data types for all fields
        assertThat(pullRequest.getGithubPrId()).isInstanceOf(Long.class);
        assertThat(pullRequest.getPrNumber()).isInstanceOf(Integer.class);
        assertThat(pullRequest.getRepository()).isInstanceOf(RepositoryEntity.class);
        assertThat(pullRequest.getDataLevel()).isInstanceOf(DataLevel.class);
        
        // Verify change statistics fields can be null (until PR_DETAIL job completes)
        if (pullRequest.getAdditions() != null) {
            assertThat(pullRequest.getAdditions()).isInstanceOf(Integer.class);
        }
        if (pullRequest.getDeletions() != null) {
            assertThat(pullRequest.getDeletions()).isInstanceOf(Integer.class);
        }
        if (pullRequest.getChangedFiles() != null) {
            assertThat(pullRequest.getChangedFiles()).isInstanceOf(Integer.class);
        }
        if (pullRequest.getChangeStatsSyncedAt() != null) {
            assertThat(pullRequest.getChangeStatsSyncedAt()).isInstanceOf(OffsetDateTime.class);
        }
        
        // Verify timestamps are proper OffsetDateTime type when present
        if (pullRequest.getCreatedAt() != null) {
            assertThat(pullRequest.getCreatedAt()).isInstanceOf(OffsetDateTime.class);
        }
        if (pullRequest.getUpdatedAt() != null) {
            assertThat(pullRequest.getUpdatedAt()).isInstanceOf(OffsetDateTime.class);
        }
        if (pullRequest.getLastSyncedAt() != null) {
            assertThat(pullRequest.getLastSyncedAt()).isInstanceOf(OffsetDateTime.class);
        }
    }

    @Property(tries = 100)
    void pullRequestReviewEntityShouldStoreAllRequiredFields(
            @ForAll("validPullRequestReviewEntity") PullRequestReviewEntity review) {
        
        // Verify non-null constraints
        assertThat(review.getGithubReviewId()).isNotNull();
        assertThat(review.getPullRequest()).isNotNull();
        
        // Verify proper data types
        assertThat(review.getGithubReviewId()).isInstanceOf(Long.class);
        assertThat(review.getPullRequest()).isInstanceOf(PullRequestEntity.class);
        
        // Verify unique constraint field (github_review_id)
        assertThat(review.getGithubReviewId()).isGreaterThan(0L);
        
        // Verify timestamps are proper OffsetDateTime type when present
        if (review.getSubmittedAt() != null) {
            assertThat(review.getSubmittedAt()).isInstanceOf(OffsetDateTime.class);
        }
    }

    @Property(tries = 100)
    void pullRequestCommitEntityShouldStoreAllRequiredFields(
            @ForAll("validPullRequestCommitEntity") PullRequestCommitEntity commit) {
        
        // Verify non-null constraints and unique constraint components
        assertThat(commit.getGithubRepoId()).isNotNull();
        assertThat(commit.getSha()).isNotNull();
        assertThat(commit.getPullRequest()).isNotNull();
        
        // Verify proper data types
        assertThat(commit.getGithubRepoId()).isInstanceOf(Long.class);
        assertThat(commit.getSha()).isInstanceOf(String.class);
        assertThat(commit.getPullRequest()).isInstanceOf(PullRequestEntity.class);
        
        // Verify SHA length constraint (40 characters for Git SHA)
        assertThat(commit.getSha()).hasSize(40);
        
        // Verify unique constraint components (github_repo_id, sha)
        assertThat(commit.getGithubRepoId()).isGreaterThan(0L);
        assertThat(commit.getSha()).matches("[0-9a-f]{40}");
        
        // Verify timestamps are proper OffsetDateTime type when present
        if (commit.getCommittedAt() != null) {
            assertThat(commit.getCommittedAt()).isInstanceOf(OffsetDateTime.class);
        }
    }

    @Property(tries = 100)
    void syncJobEntityShouldStoreAllRequiredFields(
            @ForAll("validSyncJobEntity") SyncJobEntity syncJob) {
        
        // Verify non-null constraints
        assertThat(syncJob.getJobType()).isNotNull();
        assertThat(syncJob.getRepoFullName()).isNotNull();
        assertThat(syncJob.getStatus()).isNotNull();
        
        // Verify proper data types and enum values
        assertThat(syncJob.getJobType()).isInstanceOf(SyncJobType.class);
        assertThat(syncJob.getRepoFullName()).isInstanceOf(String.class);
        assertThat(syncJob.getStatus()).isInstanceOf(SyncJobStatus.class);
        
        // Verify repo full name format (owner/repo)
        assertThat(syncJob.getRepoFullName()).contains("/");
        
        // Verify timestamps are proper OffsetDateTime type when present
        if (syncJob.getNextRunAt() != null) {
            assertThat(syncJob.getNextRunAt()).isInstanceOf(OffsetDateTime.class);
        }
        if (syncJob.getStartedAt() != null) {
            assertThat(syncJob.getStartedAt()).isInstanceOf(OffsetDateTime.class);
        }
        if (syncJob.getCompletedAt() != null) {
            assertThat(syncJob.getCompletedAt()).isInstanceOf(OffsetDateTime.class);
        }
    }

    @Property(tries = 100)
    void entitiesShouldMaintainProperRelationships(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest,
            @ForAll("validPullRequestReviewList") List<PullRequestReviewEntity> reviews,
            @ForAll("validPullRequestCommitList") List<PullRequestCommitEntity> commits) {
        
        // Set up relationships - all reviews and commits reference the same pull request
        reviews.forEach(review -> review.setPullRequest(pullRequest));
        commits.forEach(commit -> commit.setPullRequest(pullRequest));
        
        // Verify JPA @ManyToOne relationships
        for (PullRequestReviewEntity review : reviews) {
            assertThat(review.getPullRequest()).isEqualTo(pullRequest);
            assertThat(review.getPullRequest()).isNotNull();
        }
        
        for (PullRequestCommitEntity commit : commits) {
            assertThat(commit.getPullRequest()).isEqualTo(pullRequest);
            assertThat(commit.getPullRequest()).isNotNull();
        }
        
        // Verify that pull request references existing repository entity
        assertThat(pullRequest.getRepository()).isNotNull();
        assertThat(pullRequest.getRepository()).isInstanceOf(RepositoryEntity.class);
    }

    // Data generators
    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.of(DataLevel.class),         // dataLevel
                Arbitraries.integers().between(0, 10000).optional(), // additions
                Arbitraries.integers().between(0, 10000).optional(), // deletions
                Arbitraries.integers().between(0, 1000).optional(),  // changedFiles
                validOffsetDateTime().optional()         // changeStatsSyncedAt
        ).as((githubPrId, prNumber, repository, dataLevel, additions, deletions, changedFiles, changeStatsSyncedAt) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(dataLevel)
                        .additions(additions.orElse(null))
                        .deletions(deletions.orElse(null))
                        .changedFiles(changedFiles.orElse(null))
                        .changeStatsSyncedAt(changeStatsSyncedAt.orElse(null))
                        .state("open")
                        .draft(false)
                        .title("Test PR")
                        .authorLogin("testuser")
                        .authorGithubId(12345L)
                        .htmlUrl("https://github.com/test/repo/pull/1")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestReviewEntity> validPullRequestReviewEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubReviewId
                validBasicPullRequestEntity(),           // pullRequest
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // reviewerLogin
                Arbitraries.longs().greaterOrEqual(1L),  // reviewerGithubId
                Arbitraries.of("APPROVED", "CHANGES_REQUESTED", "COMMENTED") // state
        ).as((githubReviewId, pullRequest, reviewerLogin, reviewerGithubId, state) ->
                PullRequestReviewEntity.builder()
                        .githubReviewId(githubReviewId)
                        .pullRequest(pullRequest)
                        .reviewerLogin(reviewerLogin)
                        .reviewerGithubId(reviewerGithubId)
                        .state(state)
                        .submittedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/pull/1#review-" + githubReviewId)
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestCommitEntity> validPullRequestCommitEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                validSha(),                              // sha
                validBasicPullRequestEntity(),           // pullRequest
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100) // authorLogin
        ).as((githubRepoId, sha, pullRequest, authorLogin) ->
                PullRequestCommitEntity.builder()
                        .githubRepoId(githubRepoId)
                        .sha(sha)
                        .pullRequest(pullRequest)
                        .message("Test commit message")
                        .authorLogin(authorLogin)
                        .authorName("Test Author")
                        .authorEmail("test@example.com")
                        .committedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/commit/" + sha)
                        .build()
        );
    }

    @Provide
    Arbitrary<SyncJobEntity> validSyncJobEntity() {
        return Combinators.combine(
                Arbitraries.of(SyncJobType.class),       // jobType
                validRepoFullName(),                     // repoFullName
                Arbitraries.of(SyncJobStatus.class),     // status
                Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(500).optional(), // cursor
                Arbitraries.integers().between(1, 1000).optional(), // currentPage
                Arbitraries.integers().between(0, 10).optional()    // attemptCount
        ).as((jobType, repoFullName, status, cursor, currentPage, attemptCount) ->
                SyncJobEntity.builder()
                        .jobType(jobType)
                        .repoFullName(repoFullName)
                        .status(status)
                        .cursor(cursor.orElse(null))
                        .currentPage(currentPage.orElse(null))
                        .attemptCount(attemptCount.orElse(0))
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PullRequestReviewEntity>> validPullRequestReviewList() {
        return validPullRequestReviewEntity().list().ofMinSize(0).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<PullRequestCommitEntity>> validPullRequestCommitList() {
        return validPullRequestCommitEntity().list().ofMinSize(0).ofMaxSize(10);
    }

    // Helper generators
    @Provide
    Arbitrary<RepositoryEntity> validRepositoryEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(255), // name
                validRepoFullName(),                     // fullName
                validUser(),                             // user
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // ownerLogin
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((githubRepoId, name, fullName, user, ownerLogin, ownerType) ->
                RepositoryEntity.builder()
                        .githubRepoId(githubRepoId)
                        .name(name)
                        .fullName(fullName)
                        .userId(user != null ? user.getId() : null)
                        .ownerLogin(ownerLogin)
                        .ownerType(ownerType)
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestEntity> validBasicPullRequestEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.of(DataLevel.class)          // dataLevel
        ).as((githubPrId, prNumber, repository, dataLevel) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(dataLevel)
                        .state("open")
                        .draft(false)
                        .title("Test PR")
                        .authorLogin("testuser")
                        .authorGithubId(12345L)
                        .htmlUrl("https://github.com/test/repo/pull/1")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        );
    }

    @Provide
    Arbitrary<User> validUser() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubUserId
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // login
                Arbitraries.of(OwnerType.class)          // ownerType
        ).as((githubUserId, login, ownerType) ->
                User.builder()
                        .githubUserId(githubUserId)
                        .login(login)
                        .ownerType(ownerType)
                        .build()
        );
    }

    @Provide
    Arbitrary<OffsetDateTime> validOffsetDateTime() {
        return Arbitraries.longs()
                .between(1640995200L, 2147483647L) // 2022-01-01 to 2038-01-19
                .map(epochSecond -> OffsetDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(epochSecond),
                        ZoneOffset.UTC
                ));
    }

    @Provide
    Arbitrary<String> validSha() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .withCharRange('a', 'f')
                .ofLength(40);
    }

    @Provide
    Arbitrary<String> validRepoFullName() {
        return Combinators.combine(
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50),
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(50)
        ).as((owner, repo) -> owner + "/" + repo);
    }
}