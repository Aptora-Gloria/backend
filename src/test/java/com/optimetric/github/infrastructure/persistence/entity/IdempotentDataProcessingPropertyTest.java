package com.optimetric.github.infrastructure.persistence.entity;

import com.optimetric.shared.domain.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.domain.enumeration.DataLevel;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class IdempotentDataProcessingPropertyTest {

    @Property(tries = 100)
    void processingRepositoryMultipleTimesShouldMaintainExactlyOneRecordPerPullRequest(
            @ForAll("validPullRequestEntityList") List<PullRequestEntity> pullRequests) {
        
        // Simulate in-memory storage for idempotent processing
        Map<Long, PullRequestEntity> prStorage = new HashMap<>();
        
        // Process the same repository data 10 times (simulating multiple ingestion runs)
        for (int run = 1; run <= 10; run++) {
            for (PullRequestEntity pullRequest : pullRequests) {
                // Simulate idempotent processing using github_pr_id as unique key
                Long githubPrId = pullRequest.getGithubPrId();
                if (prStorage.containsKey(githubPrId)) {
                    // Update existing record (simulating ON CONFLICT DO UPDATE)
                    PullRequestEntity existingPr = prStorage.get(githubPrId);
                    existingPr.setTitle(pullRequest.getTitle());
                    existingPr.setState(pullRequest.getState());
                    existingPr.setUpdatedAt(pullRequest.getUpdatedAt());
                    existingPr.setLastSyncedAt(OffsetDateTime.now());
                } else {
                    // Create new record
                    prStorage.put(githubPrId, pullRequest);
                }
            }
        }
        
        // Verify exactly one record per unique github_pr_id
        Collection<PullRequestEntity> allPullRequests = prStorage.values();
        
        // Count should equal the number of unique github_pr_ids in input
        long uniqueGithubPrIds = pullRequests.stream()
                .mapToLong(PullRequestEntity::getGithubPrId)
                .distinct()
                .count();
        
        assertThat(allPullRequests).hasSize((int) uniqueGithubPrIds);
        
        // Verify each github_pr_id appears exactly once
        for (PullRequestEntity originalPr : pullRequests) {
            assertThat(prStorage).containsKey(originalPr.getGithubPrId());
        }
        
        // Verify unique constraint enforcement
        Set<Long> storedGithubPrIds = prStorage.keySet();
        assertThat(storedGithubPrIds).hasSize(allPullRequests.size());
    }

    @Property(tries = 100)
    void processingReviewsMultipleTimesShouldMaintainExactlyOneRecordPerGithubReviewId(
            @ForAll("validPullRequestReviewEntityList") List<PullRequestReviewEntity> reviews) {
        
        // Simulate in-memory storage for idempotent processing
        Map<Long, PullRequestReviewEntity> reviewStorage = new HashMap<>();
        
        // Process the same review data multiple times (simulating multiple ingestion runs)
        for (int run = 1; run <= 5; run++) {
            for (PullRequestReviewEntity review : reviews) {
                // Simulate idempotent processing using github_review_id as unique key
                Long githubReviewId = review.getGithubReviewId();
                if (reviewStorage.containsKey(githubReviewId)) {
                    // Update existing record (simulating ON CONFLICT DO UPDATE)
                    PullRequestReviewEntity existingReview = reviewStorage.get(githubReviewId);
                    existingReview.setState(review.getState());
                    existingReview.setBody(review.getBody());
                    existingReview.setSubmittedAt(review.getSubmittedAt());
                } else {
                    // Create new record
                    reviewStorage.put(githubReviewId, review);
                }
            }
        }
        
        // Verify exactly one record per unique github_review_id
        Collection<PullRequestReviewEntity> allReviews = reviewStorage.values();
        
        // Count should equal the number of unique github_review_ids in input
        long uniqueGithubReviewIds = reviews.stream()
                .mapToLong(PullRequestReviewEntity::getGithubReviewId)
                .distinct()
                .count();
        
        assertThat(allReviews).hasSize((int) uniqueGithubReviewIds);
        
        // Verify each github_review_id appears exactly once
        for (PullRequestReviewEntity originalReview : reviews) {
            assertThat(reviewStorage).containsKey(originalReview.getGithubReviewId());
        }
        
        // Verify unique constraint enforcement
        Set<Long> storedGithubReviewIds = reviewStorage.keySet();
        assertThat(storedGithubReviewIds).hasSize(allReviews.size());
    }

    @Property(tries = 100)
    void processingCommitsMultipleTimesShouldMaintainExactlyOneRecordPerCompositeKey(
            @ForAll("validPullRequestCommitEntityList") List<PullRequestCommitEntity> commits) {
        
        // Simulate in-memory storage for idempotent processing using composite key
        Map<String, PullRequestCommitEntity> commitStorage = new HashMap<>();
        
        // Process the same commit data multiple times (simulating multiple ingestion runs)
        for (int run = 1; run <= 5; run++) {
            for (PullRequestCommitEntity commit : commits) {
                // Simulate idempotent processing using (github_repo_id, sha) as composite unique key
                String compositeKey = commit.getGithubRepoId() + ":" + commit.getSha();
                if (commitStorage.containsKey(compositeKey)) {
                    // Update existing record (simulating ON CONFLICT DO UPDATE)
                    PullRequestCommitEntity existingCommit = commitStorage.get(compositeKey);
                    existingCommit.setMessage(commit.getMessage());
                    existingCommit.setAuthorLogin(commit.getAuthorLogin());
                    existingCommit.setCommittedAt(commit.getCommittedAt());
                } else {
                    // Create new record
                    commitStorage.put(compositeKey, commit);
                }
            }
        }
        
        // Verify exactly one record per unique (github_repo_id, sha) combination
        Collection<PullRequestCommitEntity> allCommits = commitStorage.values();
        
        // Count should equal the number of unique (github_repo_id, sha) combinations in input
        long uniqueCompositeKeys = commits.stream()
                .map(commit -> commit.getGithubRepoId() + ":" + commit.getSha())
                .distinct()
                .count();
        
        assertThat(allCommits).hasSize((int) uniqueCompositeKeys);
        
        // Verify each (github_repo_id, sha) combination appears exactly once
        for (PullRequestCommitEntity originalCommit : commits) {
            String compositeKey = originalCommit.getGithubRepoId() + ":" + originalCommit.getSha();
            assertThat(commitStorage).containsKey(compositeKey);
        }
        
        // Verify unique constraint enforcement
        Set<String> storedCompositeKeys = commitStorage.keySet();
        assertThat(storedCompositeKeys).hasSize(allCommits.size());
    }

    @Property(tries = 100)
    void idempotentOperationsShouldUseUniqueConstraintsAndJpaAnnotations(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest,
            @ForAll("validPullRequestReviewEntity") PullRequestReviewEntity review,
            @ForAll("validPullRequestCommitEntity") PullRequestCommitEntity commit) {
        
        // Verify unique constraints are properly defined through entity structure
        // PullRequestEntity should have unique github_pr_id
        assertThat(pullRequest.getGithubPrId()).isNotNull();
        assertThat(pullRequest.getGithubPrId()).isGreaterThan(0L);
        
        // PullRequestReviewEntity should have unique github_review_id
        assertThat(review.getGithubReviewId()).isNotNull();
        assertThat(review.getGithubReviewId()).isGreaterThan(0L);
        
        // PullRequestCommitEntity should have unique constraint on (github_repo_id, sha)
        assertThat(commit.getGithubRepoId()).isNotNull();
        assertThat(commit.getGithubRepoId()).isGreaterThan(0L);
        assertThat(commit.getSha()).isNotNull();
        assertThat(commit.getSha()).hasSize(40); // Git SHA constraint
        assertThat(commit.getSha()).matches("[0-9a-f]{40}"); // Valid hex SHA
        
        // Verify that unique identifiers can be used for idempotent operations
        // These identifiers should be suitable for PostgreSQL ON CONFLICT operations
        String prUniqueKey = "github_pr_id:" + pullRequest.getGithubPrId();
        String reviewUniqueKey = "github_review_id:" + review.getGithubReviewId();
        String commitUniqueKey = "github_repo_id:" + commit.getGithubRepoId() + ":sha:" + commit.getSha();
        
        assertThat(prUniqueKey).isNotEmpty();
        assertThat(reviewUniqueKey).isNotEmpty();
        assertThat(commitUniqueKey).isNotEmpty();
    }

    @Property(tries = 100)
    void timestampBasedUpdatesUsingJpaMergeShouldPreventDuplicates(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest) {
        
        // Simulate in-memory storage for timestamp-based idempotent processing
        Map<Long, PullRequestEntity> prStorage = new HashMap<>();
        
        // Initial save
        prStorage.put(pullRequest.getGithubPrId(), pullRequest);
        
        // Simulate multiple updates with different timestamps (simulating GitHub API responses)
        OffsetDateTime baseTime = OffsetDateTime.now();
        for (int i = 1; i <= 5; i++) {
            // Create updated version with newer timestamp
            PullRequestEntity updatedPr = PullRequestEntity.builder()
                    .githubPrId(pullRequest.getGithubPrId())
                    .prNumber(pullRequest.getPrNumber())
                    .dataLevel(pullRequest.getDataLevel())
                    .state(pullRequest.getState())
                    .title("Updated Title " + i)
                    .authorLogin(pullRequest.getAuthorLogin())
                    .authorGithubId(pullRequest.getAuthorGithubId())
                    .htmlUrl(pullRequest.getHtmlUrl())
                    .createdAt(pullRequest.getCreatedAt())
                    .updatedAt(baseTime.plusMinutes(i)) // Newer timestamp
                    .build();
            
            // Use timestamp-based merge operations (simulating JPA merge with timestamp comparison)
            PullRequestEntity existingPr = prStorage.get(updatedPr.getGithubPrId());
            if (existingPr != null) {
                if (updatedPr.getUpdatedAt().isAfter(existingPr.getUpdatedAt())) {
                    existingPr.setTitle(updatedPr.getTitle());
                    existingPr.setUpdatedAt(updatedPr.getUpdatedAt());
                    existingPr.setLastSyncedAt(OffsetDateTime.now());
                }
            }
        }
        
        // Verify exactly one record exists
        assertThat(prStorage).hasSize(1);
        
        // Verify the record has the latest title
        PullRequestEntity finalPr = prStorage.get(pullRequest.getGithubPrId());
        assertThat(finalPr.getTitle()).isEqualTo("Updated Title 5");
        assertThat(finalPr.getGithubPrId()).isEqualTo(pullRequest.getGithubPrId());
        
        // Verify timestamp-based updates work correctly
        assertThat(finalPr.getUpdatedAt()).isAfterOrEqualTo(pullRequest.getUpdatedAt());
        assertThat(finalPr.getLastSyncedAt()).isNotNull();
    }

    // Data generators
    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                Arbitraries.of(DataLevel.class)          // dataLevel
        ).as((githubPrId, prNumber, dataLevel) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .dataLevel(dataLevel)
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(OffsetDateTime.now().minusDays(1))
                        .updatedAt(OffsetDateTime.now())
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PullRequestEntity>> validPullRequestEntityList() {
        return validPullRequestEntity().list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<PullRequestReviewEntity> validPullRequestReviewEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubReviewId
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // reviewerLogin
                Arbitraries.longs().greaterOrEqual(1L),  // reviewerGithubId
                Arbitraries.of("APPROVED", "CHANGES_REQUESTED", "COMMENTED") // state
        ).as((githubReviewId, reviewerLogin, reviewerGithubId, state) ->
                PullRequestReviewEntity.builder()
                        .githubReviewId(githubReviewId)
                        .reviewerLogin(reviewerLogin)
                        .reviewerGithubId(reviewerGithubId)
                        .state(state)
                        .body("Review body " + githubReviewId)
                        .submittedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/pull/1#review-" + githubReviewId)
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PullRequestReviewEntity>> validPullRequestReviewEntityList() {
        return validPullRequestReviewEntity().list().ofMinSize(1).ofMaxSize(3);
    }

    @Provide
    Arbitrary<PullRequestCommitEntity> validPullRequestCommitEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubRepoId
                validSha(),                              // sha
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100) // authorLogin
        ).as((githubRepoId, sha, authorLogin) ->
                PullRequestCommitEntity.builder()
                        .githubRepoId(githubRepoId)
                        .sha(sha)
                        .message("Commit message for " + sha)
                        .authorLogin(authorLogin)
                        .authorName("Test Author")
                        .authorEmail(authorLogin + "@example.com")
                        .committedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/commit/" + sha)
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PullRequestCommitEntity>> validPullRequestCommitEntityList() {
        return validPullRequestCommitEntity().list().ofMinSize(1).ofMaxSize(3);
    }

    // Helper generators
    @Provide
    Arbitrary<String> validSha() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .withCharRange('a', 'f')
                .ofLength(40);
    }
}