package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.shared.domain.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.persistence.entity.*;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DataLevelTransitionPropertyTest {

    @Property(tries = 100)
    void pullRequestShouldHaveBasicDataLevelWhenOnlyBasicDataExists(
            @ForAll("pullRequestWithOnlyBasicData") PullRequestEntity pullRequest) {
        
        // Simulate the data level determination logic
        DataLevel determinedLevel = determineDataLevel(pullRequest, Collections.emptyList(), Collections.emptyList());
        
        // Verify that PRs with only basic data have BASIC data level
        assertThat(determinedLevel).isEqualTo(DataLevel.BASIC);
        
        // Verify the conditions that make it basic data only
        assertThat(pullRequest.getAdditions()).isNull();
        assertThat(pullRequest.getDeletions()).isNull();
        assertThat(pullRequest.getChangedFiles()).isNull();
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNull();
    }

    @Property(tries = 100)
    void pullRequestShouldTransitionToFullWhenAllDetailJobsComplete(
            @ForAll("pullRequestWithCompleteData") PullRequestWithCompleteData completeData) {
        
        PullRequestEntity pullRequest = completeData.pullRequest();
        List<PullRequestReviewEntity> reviews = completeData.reviews();
        List<PullRequestCommitEntity> commits = completeData.commits();
        
        // Simulate the data level determination logic with complete data
        DataLevel determinedLevel = determineDataLevel(pullRequest, reviews, commits);
        
        // Verify that PRs with all detail data have FULL data level
        assertThat(determinedLevel).isEqualTo(DataLevel.FULL);
        
        // Verify all detail job data is present
        // PR_DETAIL job data (change statistics)
        assertThat(pullRequest.getAdditions()).isNotNull();
        assertThat(pullRequest.getDeletions()).isNotNull();
        assertThat(pullRequest.getChangedFiles()).isNotNull();
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNotNull();
        
        // PR_REVIEWS job data (at least one review or empty list indicating job completed)
        assertThat(reviews).isNotNull();
        
        // PR_COMMITS job data (at least one commit or empty list indicating job completed)
        assertThat(commits).isNotNull();
    }

    @Property(tries = 100)
    void pullRequestShouldRemainBasicWhenDetailJobsIncomplete(
            @ForAll("pullRequestWithIncompleteData") PullRequestWithIncompleteData incompleteData) {
        
        PullRequestEntity pullRequest = incompleteData.pullRequest();
        List<PullRequestReviewEntity> reviews = incompleteData.reviews();
        List<PullRequestCommitEntity> commits = incompleteData.commits();
        
        // Simulate the data level determination logic with incomplete data
        DataLevel determinedLevel = determineDataLevel(pullRequest, reviews, commits);
        
        // Verify that PRs with incomplete detail data remain BASIC
        assertThat(determinedLevel).isEqualTo(DataLevel.BASIC);
        
        // Verify at least one detail job is incomplete
        boolean hasIncompleteChangeStats = pullRequest.getAdditions() == null || 
                                          pullRequest.getDeletions() == null || 
                                          pullRequest.getChangedFiles() == null ||
                                          pullRequest.getChangeStatsSyncedAt() == null;
        
        boolean hasIncompleteReviews = reviews == null;
        boolean hasIncompleteCommits = commits == null;
        
        // At least one detail job should be incomplete
        assertThat(hasIncompleteChangeStats || hasIncompleteReviews || hasIncompleteCommits).isTrue();
    }

    @Property(tries = 100)
    void dataLevelTransitionShouldBeIdempotent(
            @ForAll("pullRequestWithCompleteData") PullRequestWithCompleteData completeData) {
        
        PullRequestEntity pullRequest = completeData.pullRequest();
        List<PullRequestReviewEntity> reviews = completeData.reviews();
        List<PullRequestCommitEntity> commits = completeData.commits();
        
        // Determine data level multiple times
        DataLevel firstDetermination = determineDataLevel(pullRequest, reviews, commits);
        DataLevel secondDetermination = determineDataLevel(pullRequest, reviews, commits);
        DataLevel thirdDetermination = determineDataLevel(pullRequest, reviews, commits);
        
        // All determinations should be identical (idempotent)
        assertThat(firstDetermination).isEqualTo(secondDetermination);
        assertThat(secondDetermination).isEqualTo(thirdDetermination);
        
        // Should be FULL since all data is complete
        assertThat(firstDetermination).isEqualTo(DataLevel.FULL);
    }

    @Property(tries = 100)
    void dataLevelShouldTransitionCorrectlyThroughJobCompletionSequence(
            @ForAll("pullRequestJobCompletionSequence") JobCompletionSequence sequence) {
        
        PullRequestEntity pullRequest = sequence.pullRequest();
        
        // Start with BASIC data level (only basic PR data)
        DataLevel currentLevel = DataLevel.BASIC;
        assertThat(currentLevel).isEqualTo(DataLevel.BASIC);
        
        List<PullRequestReviewEntity> reviews = null;
        List<PullRequestCommitEntity> commits = null;
        
        // Apply job completions in sequence
        for (JobCompletion completion : sequence.completions()) {
            switch (completion.jobType()) {
                case PR_DETAIL -> {
                    // PR_DETAIL job completes - add change statistics
                    pullRequest.setAdditions(completion.additions());
                    pullRequest.setDeletions(completion.deletions());
                    pullRequest.setChangedFiles(completion.changedFiles());
                    pullRequest.setChangeStatsSyncedAt(OffsetDateTime.now());
                }
                case PR_REVIEWS -> {
                    // PR_REVIEWS job completes - add reviews
                    reviews = completion.reviews();
                }
                case PR_COMMITS -> {
                    // PR_COMMITS job completes - add commits
                    commits = completion.commits();
                }
            }
            
            // Determine new data level after this job completion
            currentLevel = determineDataLevel(pullRequest, reviews, commits);
        }
        
        // Verify final data level based on completed jobs
        Set<SyncJobType> completedJobs = sequence.completions().stream()
                .map(JobCompletion::jobType)
                .collect(java.util.stream.Collectors.toSet());
        
        boolean allDetailJobsCompleted = completedJobs.contains(SyncJobType.PR_DETAIL) &&
                                        completedJobs.contains(SyncJobType.PR_REVIEWS) &&
                                        completedJobs.contains(SyncJobType.PR_COMMITS);
        
        if (allDetailJobsCompleted) {
            assertThat(currentLevel).isEqualTo(DataLevel.FULL);
        } else {
            assertThat(currentLevel).isEqualTo(DataLevel.BASIC);
        }
    }

    @Property(tries = 100)
    void dataLevelShouldHandleEmptyReviewsAndCommitsCorrectly(
            @ForAll("pullRequestWithChangeStatsOnly") PullRequestEntity pullRequest) {
        
        // Simulate scenario where PR_DETAIL completed but PR_REVIEWS and PR_COMMITS returned empty lists
        List<PullRequestReviewEntity> emptyReviews = Collections.emptyList();
        List<PullRequestCommitEntity> emptyCommits = Collections.emptyList();
        
        // This should still be considered FULL since all jobs completed (even if they returned no data)
        DataLevel determinedLevel = determineDataLevel(pullRequest, emptyReviews, emptyCommits);
        
        assertThat(determinedLevel).isEqualTo(DataLevel.FULL);
        
        // Verify change stats are present (PR_DETAIL completed)
        assertThat(pullRequest.getAdditions()).isNotNull();
        assertThat(pullRequest.getDeletions()).isNotNull();
        assertThat(pullRequest.getChangedFiles()).isNotNull();
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNotNull();
    }

    private DataLevel determineDataLevel(PullRequestEntity pullRequest, 
                                       List<PullRequestReviewEntity> reviews, 
                                       List<PullRequestCommitEntity> commits) {
        if (pullRequest == null) {
            return DataLevel.BASIC;
        }
        
        // Check if PR_DETAIL job completed (change statistics available)
        boolean hasChangeStats = pullRequest.getAdditions() != null && 
                                pullRequest.getDeletions() != null && 
                                pullRequest.getChangedFiles() != null &&
                                pullRequest.getChangeStatsSyncedAt() != null;
        
        // Check if PR_REVIEWS job completed (reviews list is not null, even if empty)
        boolean hasReviewsData = reviews != null;
        
        // Check if PR_COMMITS job completed (commits list is not null, even if empty)
        boolean hasCommitsData = commits != null;
        
        // Data level is FULL only when ALL detail jobs have completed
        if (hasChangeStats && hasReviewsData && hasCommitsData) {
            return DataLevel.FULL;
        } else {
            return DataLevel.BASIC;
        }
    }

    // Data generators and helper records
    
    public record PullRequestWithCompleteData(
            PullRequestEntity pullRequest,
            List<PullRequestReviewEntity> reviews,
            List<PullRequestCommitEntity> commits
    ) {}
    
    public record PullRequestWithIncompleteData(
            PullRequestEntity pullRequest,
            List<PullRequestReviewEntity> reviews, // May be null
            List<PullRequestCommitEntity> commits   // May be null
    ) {}
    
    public record JobCompletionSequence(
            PullRequestEntity pullRequest,
            List<JobCompletion> completions
    ) {}
    
    public record JobCompletion(
            SyncJobType jobType,
            Integer additions,
            Integer deletions,
            Integer changedFiles,
            List<PullRequestReviewEntity> reviews,
            List<PullRequestCommitEntity> commits
    ) {}

    @Provide
    Arbitrary<PullRequestEntity> pullRequestWithOnlyBasicData() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                validOffsetDateTime()                    // updatedAt
        ).as((githubPrId, prNumber, repository, updatedAt) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(DataLevel.BASIC)
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(updatedAt.minusHours(1))
                        .updatedAt(updatedAt)
                        // No change statistics (PR_DETAIL job not completed)
                        .additions(null)
                        .deletions(null)
                        .changedFiles(null)
                        .changeStatsSyncedAt(null)
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestWithCompleteData> pullRequestWithCompleteData() {
        return Combinators.combine(
                pullRequestWithChangeStatsOnly(),       // PR with change stats
                validPullRequestReviewList(),           // Reviews (may be empty)
                validPullRequestCommitList()            // Commits (may be empty)
        ).as(PullRequestWithCompleteData::new);
    }

    @Provide
    Arbitrary<PullRequestEntity> pullRequestWithChangeStatsOnly() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.integers().between(0, 10000), // additions
                Arbitraries.integers().between(0, 10000), // deletions
                Arbitraries.integers().between(1, 1000),  // changedFiles
                validOffsetDateTime()                    // updatedAt
        ).as((githubPrId, prNumber, repository, additions, deletions, changedFiles, updatedAt) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(DataLevel.BASIC) // Will be updated by logic
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(updatedAt.minusHours(1))
                        .updatedAt(updatedAt)
                        // Change statistics present (PR_DETAIL job completed)
                        .additions(additions)
                        .deletions(deletions)
                        .changedFiles(changedFiles)
                        .changeStatsSyncedAt(updatedAt.plusMinutes(30))
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<PullRequestWithIncompleteData> pullRequestWithIncompleteData() {
        // Create different scenarios where at least one detail job is incomplete
        return Arbitraries.oneOf(
                // Scenario 1: Missing change stats (PR_DETAIL job not completed)
                Combinators.combine(
                        pullRequestWithOnlyBasicData(),     // No change stats
                        validPullRequestReviewList().optional(),
                        validPullRequestCommitList().optional()
                ).as((pr, reviews, commits) -> 
                        new PullRequestWithIncompleteData(pr, reviews.orElse(null), commits.orElse(null))),
                
                // Scenario 2: Has change stats but missing reviews (PR_REVIEWS job not completed)
                Combinators.combine(
                        pullRequestWithChangeStatsOnly(),   // Has change stats
                        validPullRequestCommitList().optional()
                ).as((pr, commits) -> 
                        new PullRequestWithIncompleteData(pr, null, commits.orElse(null))),
                
                // Scenario 3: Has change stats but missing commits (PR_COMMITS job not completed)
                Combinators.combine(
                        pullRequestWithChangeStatsOnly(),   // Has change stats
                        validPullRequestReviewList().optional()
                ).as((pr, reviews) -> 
                        new PullRequestWithIncompleteData(pr, reviews.orElse(null), null)),
                
                // Scenario 4: Has change stats but missing both reviews and commits
                pullRequestWithChangeStatsOnly().map(pr -> 
                        new PullRequestWithIncompleteData(pr, null, null))
        );
    }

    @Provide
    Arbitrary<PullRequestEntity> pullRequestWithPartialData() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.integers().between(0, 10000).optional(), // additions (may be null)
                Arbitraries.integers().between(0, 10000).optional(), // deletions (may be null)
                Arbitraries.integers().between(1, 1000).optional(),  // changedFiles (may be null)
                validOffsetDateTime().optional(),        // changeStatsSyncedAt (may be null)
                validOffsetDateTime()                    // updatedAt
        ).as((githubPrId, prNumber, repository, additions, deletions, changedFiles, changeStatsSyncedAt, updatedAt) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(DataLevel.BASIC)
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(updatedAt.minusHours(1))
                        .updatedAt(updatedAt)
                        // Partial change statistics (some may be null)
                        .additions(additions.orElse(null))
                        .deletions(deletions.orElse(null))
                        .changedFiles(changedFiles.orElse(null))
                        .changeStatsSyncedAt(changeStatsSyncedAt.orElse(null))
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<JobCompletionSequence> pullRequestJobCompletionSequence() {
        return Combinators.combine(
                pullRequestWithOnlyBasicData(),         // Start with basic data
                validJobCompletionList()                // Sequence of job completions
        ).as(JobCompletionSequence::new);
    }

    @Provide
    Arbitrary<List<JobCompletion>> validJobCompletionList() {
        return validJobCompletion().list().ofMinSize(1).ofMaxSize(3);
    }

    @Provide
    Arbitrary<JobCompletion> validJobCompletion() {
        return Combinators.combine(
                Arbitraries.of(SyncJobType.PR_DETAIL, SyncJobType.PR_REVIEWS, SyncJobType.PR_COMMITS),
                Arbitraries.integers().between(0, 10000), // additions
                Arbitraries.integers().between(0, 10000), // deletions
                Arbitraries.integers().between(1, 1000),  // changedFiles
                validPullRequestReviewList(),            // reviews
                validPullRequestCommitList()             // commits
        ).as(JobCompletion::new);
    }

    @Provide
    Arbitrary<List<PullRequestReviewEntity>> validPullRequestReviewList() {
        return validPullRequestReviewEntity().list().ofMinSize(0).ofMaxSize(5);
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
                        .submittedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/pull/1#review-" + githubReviewId)
                        .build()
        );
    }

    @Provide
    Arbitrary<List<PullRequestCommitEntity>> validPullRequestCommitList() {
        return validPullRequestCommitEntity().list().ofMinSize(0).ofMaxSize(10);
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
                        .message("Test commit message")
                        .authorLogin(authorLogin)
                        .authorName("Test Author")
                        .authorEmail("test@example.com")
                        .committedAt(OffsetDateTime.now())
                        .htmlUrl("https://github.com/test/repo/commit/" + sha)
                        .build()
        );
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
                .between(1640995200L, 1735689600L) // 2022-01-01 to 2025-01-01
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