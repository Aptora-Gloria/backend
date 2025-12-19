package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.infrastructure.http.dto.*;
import com.optimetric.github.infrastructure.persistence.entity.*;
import net.jqwik.api.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for ingestion job execution completeness.
 * **Feature: pull-request-ingestion, Property 1: Ingestion Job Execution Completeness**
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 */
public class IngestionJobCompletenessPropertyTest {

    @Property(tries = 100)
    void prListJobShouldDiscoverAndUpsertPullRequestsWithBasicDataLevel(
            @ForAll("validRepositoryEntity") RepositoryEntity repository,
            @ForAll("validGitHubPullRequestResponseList") List<GitHubPullRequestResponse> githubPrResponses) {
        
        // Simulate PR_LIST job execution
        Map<Long, PullRequestEntity> discoveredPrs = new HashMap<>();
        
        // Process each GitHub PR response (simulating upsertBasicPullRequest logic)
        for (GitHubPullRequestResponse prResponse : githubPrResponses) {
            PullRequestEntity pullRequest = createBasicPullRequestFromResponse(repository, prResponse);
            discoveredPrs.put(pullRequest.getGithubPrId(), pullRequest);
        }
        
        // Verify all discovered PRs have BASIC data level
        for (PullRequestEntity pr : discoveredPrs.values()) {
            assertThat(pr.getDataLevel()).isEqualTo(DataLevel.BASIC);
            
            // Verify basic PR data is stored
            assertThat(pr.getGithubPrId()).isNotNull();
            assertThat(pr.getPrNumber()).isNotNull();
            assertThat(pr.getRepository()).isEqualTo(repository);
            assertThat(pr.getState()).isNotNull();
            assertThat(pr.getTitle()).isNotNull();
            assertThat(pr.getAuthorLogin()).isNotNull();
            assertThat(pr.getLastSyncedAt()).isNotNull();
            
            // Verify change statistics are not present (BASIC level)
            assertThat(pr.getAdditions()).isNull();
            assertThat(pr.getDeletions()).isNull();
            assertThat(pr.getChangedFiles()).isNull();
            assertThat(pr.getChangeStatsSyncedAt()).isNull();
        }
        
        // Verify idempotent behavior - no duplicates by github_pr_id
        Set<Long> uniqueGithubPrIds = discoveredPrs.keySet();
        Set<Long> expectedUniqueIds = githubPrResponses.stream()
                .map(GitHubPullRequestResponse::id)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(uniqueGithubPrIds).hasSize(expectedUniqueIds.size());
        
        // Verify all unique GitHub PR IDs were processed (duplicates should be deduplicated)
        for (Long expectedId : expectedUniqueIds) {
            assertThat(discoveredPrs).containsKey(expectedId);
        }
    }

    @Property(tries = 100)
    void prDetailJobShouldFetchChangeStatisticsAndUpdateDataLevel(
            @ForAll("validPullRequestEntityWithBasicData") PullRequestEntity pullRequest,
            @ForAll("validGitHubPullRequestDetailResponse") GitHubPullRequestDetailResponse detailResponse) {
        
        // Verify initial state is BASIC
        assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.BASIC);
        assertThat(pullRequest.getAdditions()).isNull();
        assertThat(pullRequest.getDeletions()).isNull();
        assertThat(pullRequest.getChangedFiles()).isNull();
        
        // Simulate PR_DETAIL job execution (updatePullRequestWithDetails logic)
        pullRequest.setAdditions(detailResponse.additions());
        pullRequest.setDeletions(detailResponse.deletions());
        pullRequest.setChangedFiles(detailResponse.changedFiles());
        pullRequest.setChangeStatsSyncedAt(OffsetDateTime.now());
        pullRequest.setLastSyncedAt(OffsetDateTime.now());
        
        // Update data level based on completeness
        updateDataLevel(pullRequest);
        
        // Verify change statistics are now present
        assertThat(pullRequest.getAdditions()).isEqualTo(detailResponse.additions());
        assertThat(pullRequest.getDeletions()).isEqualTo(detailResponse.deletions());
        assertThat(pullRequest.getChangedFiles()).isEqualTo(detailResponse.changedFiles());
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNotNull();
        
        // Verify data level is updated accordingly
        boolean hasChangeStats = pullRequest.getAdditions() != null && 
                                pullRequest.getDeletions() != null && 
                                pullRequest.getChangedFiles() != null;
        
        if (hasChangeStats) {
            assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.FULL);
        } else {
            assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.BASIC);
        }
    }

    @Property(tries = 100)
    void prReviewsJobShouldCollectAllReviewsWithUniqueGithubReviewId(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest,
            @ForAll("validGitHubPullRequestReviewResponseList") List<GitHubPullRequestReviewResponse> reviewResponses) {
        
        // Simulate PR_REVIEWS job execution
        Map<Long, PullRequestReviewEntity> collectedReviews = new HashMap<>();
        
        // Process each GitHub review response (simulating upsertPullRequestReview logic)
        for (GitHubPullRequestReviewResponse reviewResponse : reviewResponses) {
            PullRequestReviewEntity review = createReviewFromResponse(pullRequest, reviewResponse);
            collectedReviews.put(review.getGithubReviewId(), review);
        }
        
        // Verify all reviews are collected with unique github_review_id (idempotent behavior)
        Set<Long> uniqueReviewIds = collectedReviews.keySet();
        Set<Long> expectedUniqueReviewIds = reviewResponses.stream()
                .map(GitHubPullRequestReviewResponse::id)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(uniqueReviewIds).hasSize(expectedUniqueReviewIds.size());
        
        // Verify all unique GitHub review IDs were processed (duplicates should be deduplicated)
        for (Long expectedId : expectedUniqueReviewIds) {
            assertThat(collectedReviews).containsKey(expectedId);
        }
        
        // Verify review data completeness
        for (PullRequestReviewEntity review : collectedReviews.values()) {
            assertThat(review.getGithubReviewId()).isNotNull();
            assertThat(review.getPullRequest()).isEqualTo(pullRequest);
            assertThat(review.getReviewerLogin()).isNotNull();
            assertThat(review.getReviewerGithubId()).isNotNull();
            assertThat(review.getState()).isNotNull();
            assertThat(review.getSubmittedAt()).isNotNull();
            assertThat(review.getHtmlUrl()).isNotNull();
        }
        
        // Verify idempotent behavior - processing same reviews again should not create duplicates
        Map<Long, PullRequestReviewEntity> reprocessedReviews = new HashMap<>();
        for (GitHubPullRequestReviewResponse reviewResponse : reviewResponses) {
            PullRequestReviewEntity review = createReviewFromResponse(pullRequest, reviewResponse);
            reprocessedReviews.put(review.getGithubReviewId(), review);
        }
        
        assertThat(reprocessedReviews.keySet()).isEqualTo(collectedReviews.keySet());
    }

    @Property(tries = 100)
    void prCommitsJobShouldCollectAllCommitsWithUniqueCompositeKey(
            @ForAll("validPullRequestEntity") PullRequestEntity pullRequest,
            @ForAll("validGitHubPullRequestCommitResponseList") List<GitHubPullRequestCommitResponse> commitResponses) {
        
        Long githubRepoId = pullRequest.getRepository().getGithubRepoId();
        
        // Simulate PR_COMMITS job execution
        Map<String, PullRequestCommitEntity> collectedCommits = new HashMap<>();
        
        // Process each GitHub commit response (simulating upsertPullRequestCommit logic)
        for (GitHubPullRequestCommitResponse commitResponse : commitResponses) {
            PullRequestCommitEntity commit = createCommitFromResponse(pullRequest, githubRepoId, commitResponse);
            String compositeKey = githubRepoId + ":" + commit.getSha();
            collectedCommits.put(compositeKey, commit);
        }
        
        // Verify all commits are collected with unique (github_repo_id, sha) combination (idempotent behavior)
        Set<String> uniqueCompositeKeys = collectedCommits.keySet();
        Set<String> expectedUniqueKeys = commitResponses.stream()
                .map(response -> githubRepoId + ":" + response.sha())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(uniqueCompositeKeys).hasSize(expectedUniqueKeys.size());
        
        // Verify all unique GitHub commit composite keys were processed (duplicates should be deduplicated)
        for (String expectedKey : expectedUniqueKeys) {
            assertThat(collectedCommits).containsKey(expectedKey);
        }
        
        // Verify commit data completeness
        for (PullRequestCommitEntity commit : collectedCommits.values()) {
            assertThat(commit.getGithubRepoId()).isEqualTo(githubRepoId);
            assertThat(commit.getSha()).isNotNull();
            assertThat(commit.getSha()).hasSize(40); // SHA should be 40 characters
            assertThat(commit.getPullRequest()).isEqualTo(pullRequest);
            assertThat(commit.getMessage()).isNotNull();
            assertThat(commit.getAuthorLogin()).isNotNull();
            assertThat(commit.getAuthorName()).isNotNull();
            assertThat(commit.getCommittedAt()).isNotNull();
            assertThat(commit.getHtmlUrl()).isNotNull();
        }
        
        // Verify idempotent behavior - processing same commits again should not create duplicates
        Map<String, PullRequestCommitEntity> reprocessedCommits = new HashMap<>();
        for (GitHubPullRequestCommitResponse commitResponse : commitResponses) {
            PullRequestCommitEntity commit = createCommitFromResponse(pullRequest, githubRepoId, commitResponse);
            String compositeKey = githubRepoId + ":" + commit.getSha();
            reprocessedCommits.put(compositeKey, commit);
        }
        
        assertThat(reprocessedCommits.keySet()).isEqualTo(collectedCommits.keySet());
    }

    @Property(tries = 100)
    void allDetailJobsCompletionShouldMarkPullRequestDataLevelAsFull(
            @ForAll("validPullRequestEntityWithBasicData") PullRequestEntity pullRequest,
            @ForAll("validGitHubPullRequestDetailResponse") GitHubPullRequestDetailResponse detailResponse,
            @ForAll("validGitHubPullRequestReviewResponseList") List<GitHubPullRequestReviewResponse> reviewResponses,
            @ForAll("validGitHubPullRequestCommitResponseList") List<GitHubPullRequestCommitResponse> commitResponses) {
        
        // Verify initial state is BASIC
        assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.BASIC);
        
        // Simulate PR_DETAIL job completion
        pullRequest.setAdditions(detailResponse.additions());
        pullRequest.setDeletions(detailResponse.deletions());
        pullRequest.setChangedFiles(detailResponse.changedFiles());
        pullRequest.setChangeStatsSyncedAt(OffsetDateTime.now());
        
        // Simulate PR_REVIEWS job completion (collect reviews with deduplication)
        Map<Long, PullRequestReviewEntity> reviewMap = new HashMap<>();
        for (GitHubPullRequestReviewResponse reviewResponse : reviewResponses) {
            PullRequestReviewEntity review = createReviewFromResponse(pullRequest, reviewResponse);
            reviewMap.put(review.getGithubReviewId(), review);
        }
        List<PullRequestReviewEntity> reviews = new ArrayList<>(reviewMap.values());
        
        // Simulate PR_COMMITS job completion (collect commits with deduplication)
        Map<String, PullRequestCommitEntity> commitMap = new HashMap<>();
        Long githubRepoId = pullRequest.getRepository().getGithubRepoId();
        for (GitHubPullRequestCommitResponse commitResponse : commitResponses) {
            PullRequestCommitEntity commit = createCommitFromResponse(pullRequest, githubRepoId, commitResponse);
            String compositeKey = githubRepoId + ":" + commit.getSha();
            commitMap.put(compositeKey, commit);
        }
        List<PullRequestCommitEntity> commits = new ArrayList<>(commitMap.values());
        
        // Update data level based on all jobs completion
        updateDataLevel(pullRequest);
        
        // Verify that when all detail jobs complete, data level is FULL
        boolean hasChangeStats = pullRequest.getAdditions() != null && 
                                pullRequest.getDeletions() != null && 
                                pullRequest.getChangedFiles() != null &&
                                pullRequest.getChangeStatsSyncedAt() != null;
        
        boolean hasReviewsData = reviews != null; // Reviews job completed (even if empty list)
        boolean hasCommitsData = commits != null; // Commits job completed (even if empty list)
        
        if (hasChangeStats && hasReviewsData && hasCommitsData) {
            assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.FULL);
        } else {
            assertThat(pullRequest.getDataLevel()).isEqualTo(DataLevel.BASIC);
        }
        
        // Verify all data is properly stored
        assertThat(pullRequest.getAdditions()).isEqualTo(detailResponse.additions());
        assertThat(pullRequest.getDeletions()).isEqualTo(detailResponse.deletions());
        assertThat(pullRequest.getChangedFiles()).isEqualTo(detailResponse.changedFiles());
        assertThat(pullRequest.getChangeStatsSyncedAt()).isNotNull();
        
        // Verify reviews are collected (accounting for deduplication)
        Set<Long> expectedUniqueReviewIds = reviewResponses.stream()
                .map(GitHubPullRequestReviewResponse::id)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(reviews).hasSize(expectedUniqueReviewIds.size());
        Set<Long> actualReviewIds = reviews.stream()
                .map(PullRequestReviewEntity::getGithubReviewId)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(actualReviewIds).isEqualTo(expectedUniqueReviewIds);
        
        // Verify commits are collected (accounting for deduplication)
        Set<String> expectedUniqueShas = commitResponses.stream()
                .map(GitHubPullRequestCommitResponse::sha)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(commits).hasSize(expectedUniqueShas.size());
        Set<String> actualShas = commits.stream()
                .map(PullRequestCommitEntity::getSha)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(actualShas).isEqualTo(expectedUniqueShas);
    }

    @Property(tries = 100)
    void ingestionJobsShouldMaintainDataIntegrityAndRelationships(
            @ForAll("validRepositoryEntity") RepositoryEntity repository,
            @ForAll("validCompleteIngestionScenario") CompleteIngestionScenario scenario) {
        
        // Simulate complete ingestion pipeline
        Map<Long, PullRequestEntity> allPrs = new HashMap<>();
        Map<Long, List<PullRequestReviewEntity>> allReviews = new HashMap<>();
        Map<Long, List<PullRequestCommitEntity>> allCommits = new HashMap<>();
        
        // Step 1: PR_LIST job - discover PRs
        for (GitHubPullRequestResponse prResponse : scenario.pullRequestResponses()) {
            PullRequestEntity pr = createBasicPullRequestFromResponse(repository, prResponse);
            allPrs.put(pr.getGithubPrId(), pr);
        }
        
        // Step 2: Detail jobs for each unique PR (handling duplicates correctly)
        Map<Long, List<GitHubPullRequestReviewResponse>> allReviewResponsesByPrId = new HashMap<>();
        Map<Long, List<GitHubPullRequestCommitResponse>> allCommitResponsesByPrId = new HashMap<>();
        
        // Collect all reviews and commits for each unique PR ID
        for (int i = 0; i < scenario.pullRequestResponses().size(); i++) {
            GitHubPullRequestResponse prResponse = scenario.pullRequestResponses().get(i);
            Long prId = prResponse.id();
            
            // Collect reviews for this PR ID
            if (i < scenario.reviewResponsesPerPr().size()) {
                List<GitHubPullRequestReviewResponse> reviewResponses = scenario.reviewResponsesPerPr().get(i);
                allReviewResponsesByPrId.computeIfAbsent(prId, k -> new ArrayList<>()).addAll(reviewResponses);
            }
            
            // Collect commits for this PR ID
            if (i < scenario.commitResponsesPerPr().size()) {
                List<GitHubPullRequestCommitResponse> commitResponses = scenario.commitResponsesPerPr().get(i);
                allCommitResponsesByPrId.computeIfAbsent(prId, k -> new ArrayList<>()).addAll(commitResponses);
            }
        }
        
        // Process detail jobs for each unique PR
        for (PullRequestEntity pr : allPrs.values()) {
            Long prId = pr.getGithubPrId();
            
            // PR_DETAIL job (use the first detail response for this PR ID)
            for (int i = 0; i < scenario.pullRequestResponses().size(); i++) {
                if (scenario.pullRequestResponses().get(i).id().equals(prId) && i < scenario.detailResponses().size()) {
                    GitHubPullRequestDetailResponse detailResponse = scenario.detailResponses().get(i);
                    pr.setAdditions(detailResponse.additions());
                    pr.setDeletions(detailResponse.deletions());
                    pr.setChangedFiles(detailResponse.changedFiles());
                    pr.setChangeStatsSyncedAt(OffsetDateTime.now());
                    break;
                }
            }
            
            // PR_REVIEWS job (with deduplication across all reviews for this PR ID)
            if (allReviewResponsesByPrId.containsKey(prId)) {
                Map<Long, PullRequestReviewEntity> reviewMap = new HashMap<>();
                for (GitHubPullRequestReviewResponse reviewResponse : allReviewResponsesByPrId.get(prId)) {
                    PullRequestReviewEntity review = createReviewFromResponse(pr, reviewResponse);
                    reviewMap.put(review.getGithubReviewId(), review);
                }
                allReviews.put(prId, new ArrayList<>(reviewMap.values()));
            }
            
            // PR_COMMITS job (with deduplication across all commits for this PR ID)
            if (allCommitResponsesByPrId.containsKey(prId)) {
                Map<String, PullRequestCommitEntity> commitMap = new HashMap<>();
                for (GitHubPullRequestCommitResponse commitResponse : allCommitResponsesByPrId.get(prId)) {
                    PullRequestCommitEntity commit = createCommitFromResponse(pr, repository.getGithubRepoId(), commitResponse);
                    String compositeKey = repository.getGithubRepoId() + ":" + commit.getSha();
                    commitMap.put(compositeKey, commit);
                }
                allCommits.put(prId, new ArrayList<>(commitMap.values()));
            }
            
            // Update data level
            updateDataLevel(pr);
        }
        
        // Verify data integrity and relationships
        for (PullRequestEntity pr : allPrs.values()) {
            // Verify PR-Repository relationship
            assertThat(pr.getRepository()).isEqualTo(repository);
            
            // Verify data level consistency
            boolean hasChangeStats = pr.getAdditions() != null && 
                                    pr.getDeletions() != null && 
                                    pr.getChangedFiles() != null;
            
            boolean hasReviews = allReviews.containsKey(pr.getGithubPrId());
            boolean hasCommits = allCommits.containsKey(pr.getGithubPrId());
            
            if (hasChangeStats && hasReviews && hasCommits) {
                assertThat(pr.getDataLevel()).isEqualTo(DataLevel.FULL);
            } else {
                assertThat(pr.getDataLevel()).isEqualTo(DataLevel.BASIC);
            }
            
            // Verify review relationships
            if (allReviews.containsKey(pr.getGithubPrId())) {
                for (PullRequestReviewEntity review : allReviews.get(pr.getGithubPrId())) {
                    assertThat(review.getPullRequest()).isEqualTo(pr);
                }
            }
            
            // Verify commit relationships
            if (allCommits.containsKey(pr.getGithubPrId())) {
                for (PullRequestCommitEntity commit : allCommits.get(pr.getGithubPrId())) {
                    assertThat(commit.getPullRequest()).isEqualTo(pr);
                    assertThat(commit.getGithubRepoId()).isEqualTo(repository.getGithubRepoId());
                }
            }
        }
        
        // Verify no duplicate entities by unique identifiers (accounting for deduplication)
        Set<Long> uniquePrIds = allPrs.keySet();
        Set<Long> expectedUniquePrIds = scenario.pullRequestResponses().stream()
                .map(GitHubPullRequestResponse::id)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(uniquePrIds).isEqualTo(expectedUniquePrIds);
        
        // Verify unique review IDs across all PRs (accounting for deduplication)
        Set<Long> allReviewIds = allReviews.values().stream()
                .flatMap(List::stream)
                .map(PullRequestReviewEntity::getGithubReviewId)
                .collect(Collectors.toSet());
        
        Set<Long> expectedUniqueReviewIds = scenario.reviewResponsesPerPr().stream()
                .flatMap(List::stream)
                .map(GitHubPullRequestReviewResponse::id)
                .collect(Collectors.toSet());
        
        assertThat(allReviewIds).hasSize(expectedUniqueReviewIds.size());
        
        // Verify unique commit composite keys across all PRs (accounting for deduplication)
        Set<String> allCommitKeys = allCommits.values().stream()
                .flatMap(List::stream)
                .map(commit -> commit.getGithubRepoId() + ":" + commit.getSha())
                .collect(Collectors.toSet());
        
        Set<String> expectedUniqueCommitKeys = scenario.commitResponsesPerPr().stream()
                .flatMap(List::stream)
                .map(response -> repository.getGithubRepoId() + ":" + response.sha())
                .collect(Collectors.toSet());
        
        assertThat(allCommitKeys).hasSize(expectedUniqueCommitKeys.size());
    }

    // Helper methods simulating the actual service logic

    private PullRequestEntity createBasicPullRequestFromResponse(RepositoryEntity repository, GitHubPullRequestResponse response) {
        PullRequestEntity pr = new PullRequestEntity();
        pr.setGithubPrId(response.id());
        pr.setPrNumber(response.number());
        pr.setRepository(repository);
        pr.setDataLevel(DataLevel.BASIC);
        pr.setState(response.state());
        pr.setDraft(response.draft());
        pr.setTitle(response.title());
        pr.setBody(response.body());
        pr.setHtmlUrl(response.htmlUrl());
        pr.setMergeCommitSha(response.mergeCommitSha());
        pr.setCreatedAt(response.createdAt());
        pr.setUpdatedAt(response.updatedAt());
        pr.setClosedAt(response.closedAt());
        pr.setMergedAt(response.mergedAt());
        pr.setLastSyncedAt(OffsetDateTime.now());
        
        if (response.user() != null) {
            pr.setAuthorLogin(response.user().login());
            pr.setAuthorGithubId(response.user().id());
        }
        
        return pr;
    }

    private PullRequestReviewEntity createReviewFromResponse(PullRequestEntity pullRequest, GitHubPullRequestReviewResponse response) {
        PullRequestReviewEntity review = new PullRequestReviewEntity();
        review.setGithubReviewId(response.id());
        review.setPullRequest(pullRequest);
        review.setState(response.state());
        review.setBody(response.body());
        review.setSubmittedAt(response.submittedAt());
        review.setHtmlUrl(response.htmlUrl());
        
        if (response.user() != null) {
            review.setReviewerLogin(response.user().login());
            review.setReviewerGithubId(response.user().id());
        }
        
        return review;
    }

    private PullRequestCommitEntity createCommitFromResponse(PullRequestEntity pullRequest, Long githubRepoId, GitHubPullRequestCommitResponse response) {
        PullRequestCommitEntity commit = new PullRequestCommitEntity();
        commit.setGithubRepoId(githubRepoId);
        commit.setSha(response.sha());
        commit.setPullRequest(pullRequest);
        commit.setHtmlUrl(response.htmlUrl());
        
        if (response.commit() != null) {
            commit.setMessage(response.commit().message());
            if (response.commit().author() != null) {
                commit.setAuthorName(response.commit().author().name());
                commit.setCommittedAt(response.commit().author().date());
            }
        }
        
        if (response.author() != null) {
            commit.setAuthorLogin(response.author().login());
        }
        
        return commit;
    }

    private void updateDataLevel(PullRequestEntity pullRequest) {
        boolean hasChangeStats = pullRequest.getAdditions() != null && 
                                pullRequest.getDeletions() != null && 
                                pullRequest.getChangedFiles() != null;
        
        DataLevel newDataLevel = hasChangeStats ? DataLevel.FULL : DataLevel.BASIC;
        pullRequest.setDataLevel(newDataLevel);
    }

    // Data generators and helper records

    public record CompleteIngestionScenario(
            List<GitHubPullRequestResponse> pullRequestResponses,
            List<GitHubPullRequestDetailResponse> detailResponses,
            List<List<GitHubPullRequestReviewResponse>> reviewResponsesPerPr,
            List<List<GitHubPullRequestCommitResponse>> commitResponsesPerPr
    ) {}

    @Provide
    Arbitrary<PullRequestEntity> validPullRequestEntityWithBasicData() {
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
                        .lastSyncedAt(updatedAt.plusMinutes(5))
                        // No change statistics (BASIC level)
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
    Arbitrary<PullRequestEntity> validPullRequestEntity() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // githubPrId
                Arbitraries.integers().between(1, 99999), // prNumber
                validRepositoryEntity(),                  // repository
                Arbitraries.of(DataLevel.class),         // dataLevel
                validOffsetDateTime()                    // updatedAt
        ).as((githubPrId, prNumber, repository, dataLevel, updatedAt) ->
                PullRequestEntity.builder()
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .repository(repository)
                        .dataLevel(dataLevel)
                        .state("open")
                        .draft(false)
                        .title("Test PR " + githubPrId)
                        .authorLogin("testuser" + githubPrId)
                        .authorGithubId(12345L + githubPrId)
                        .htmlUrl("https://github.com/test/repo/pull/" + prNumber)
                        .createdAt(updatedAt.minusHours(1))
                        .updatedAt(updatedAt)
                        .lastSyncedAt(updatedAt.plusMinutes(5))
                        .systemCreatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .systemUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );
    }

    @Provide
    Arbitrary<List<GitHubPullRequestResponse>> validGitHubPullRequestResponseList() {
        return validGitHubPullRequestResponse().list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<GitHubPullRequestResponse> validGitHubPullRequestResponse() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.integers().between(1, 99999), // number
                Arbitraries.of("open", "closed"),        // state
                Arbitraries.of(true, false),             // draft
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(100), // title
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(0).ofMaxLength(500).optional(), // body
                validOffsetDateTime(),                   // createdAt
                validOffsetDateTime()                    // updatedAt
        ).as((id, number, state, draft, title, body, createdAt, updatedAt) -> {
            String htmlUrl = "https://github.com/test/repo/pull/" + number;
            return new GitHubPullRequestResponse(
                    id, number, state, draft, title, body.orElse(null),
                    htmlUrl, null, createdAt, updatedAt, null, null,
                    new GitHubPullRequestResponse.GitHubUserInfo(12345L + id, "testuser" + id)
            );
        });
    }

    @Provide
    Arbitrary<GitHubPullRequestDetailResponse> validGitHubPullRequestDetailResponse() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.integers().between(1, 99999), // number
                Arbitraries.of("open", "closed"),        // state
                Arbitraries.of(true, false),             // draft
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(100), // title
                Arbitraries.integers().between(0, 10000), // additions
                Arbitraries.integers().between(0, 10000), // deletions
                Arbitraries.integers().between(1, 1000)   // changedFiles
        ).as((id, number, state, draft, title, additions, deletions, changedFiles) -> {
            OffsetDateTime now = OffsetDateTime.now();
            String htmlUrl = "https://github.com/test/repo/pull/" + number;
            return new GitHubPullRequestDetailResponse(
                    id, number, state, draft, title, null,
                    htmlUrl, null, now.minusHours(1), now, null, null,
                    new GitHubPullRequestDetailResponse.GitHubUserInfo(12345L + id, "testuser" + id),
                    additions, deletions, changedFiles
            );
        });
    }

    @Provide
    Arbitrary<List<GitHubPullRequestReviewResponse>> validGitHubPullRequestReviewResponseList() {
        return validGitHubPullRequestReviewResponse().list().ofMinSize(0).ofMaxSize(5);
    }

    @Provide
    Arbitrary<GitHubPullRequestReviewResponse> validGitHubPullRequestReviewResponse() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L),  // id
                Arbitraries.of("APPROVED", "CHANGES_REQUESTED", "COMMENTED"), // state
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(0).ofMaxLength(500).optional(), // body
                validOffsetDateTime()                    // submittedAt
        ).as((id, state, body, submittedAt) -> {
            GitHubPullRequestReviewResponse.GitHubUserInfo user = 
                new GitHubPullRequestReviewResponse.GitHubUserInfo(12345L + id, "reviewer" + id);
            return new GitHubPullRequestReviewResponse(
                    id, user, state, body.orElse(null), submittedAt,
                    "https://github.com/test/repo/pull/1#review-" + id
            );
        });
    }

    @Provide
    Arbitrary<List<GitHubPullRequestCommitResponse>> validGitHubPullRequestCommitResponseList() {
        return validGitHubPullRequestCommitResponse().list().ofMinSize(0).ofMaxSize(10);
    }

    @Provide
    Arbitrary<GitHubPullRequestCommitResponse> validGitHubPullRequestCommitResponse() {
        return Combinators.combine(
                validSha(),                              // sha
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(10).ofMaxLength(200), // message
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100), // authorName
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(100)  // authorLogin
        ).as((sha, message, authorName, authorLogin) -> {
            OffsetDateTime now = OffsetDateTime.now();
            GitHubPullRequestCommitResponse.GitHubCommitAuthor commitAuthor = 
                new GitHubPullRequestCommitResponse.GitHubCommitAuthor(authorName, authorName + "@example.com", now);
            GitHubPullRequestCommitResponse.GitHubCommitInfo commit = 
                new GitHubPullRequestCommitResponse.GitHubCommitInfo(message, commitAuthor);
            GitHubPullRequestCommitResponse.GitHubUserInfo author = 
                new GitHubPullRequestCommitResponse.GitHubUserInfo(authorLogin);
            return new GitHubPullRequestCommitResponse(
                    sha, commit, "https://github.com/test/repo/commit/" + sha, author
            );
        });
    }

    @Provide
    Arbitrary<CompleteIngestionScenario> validCompleteIngestionScenario() {
        return validGitHubPullRequestResponseList().flatMap(prResponses -> {
            int prCount = prResponses.size();
            return Combinators.combine(
                    Arbitraries.just(prResponses),       // pullRequestResponses
                    validGitHubPullRequestDetailResponse().list().ofSize(prCount), // detailResponses (same size as PRs)
                    validGitHubPullRequestReviewResponseList().list().ofSize(prCount), // reviewResponsesPerPr (same size as PRs)
                    validGitHubPullRequestCommitResponseList().list().ofSize(prCount)  // commitResponsesPerPr (same size as PRs)
            ).as(CompleteIngestionScenario::new);
        });
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
                        .user(user)
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