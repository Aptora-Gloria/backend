package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.DataLevel;

import java.time.OffsetDateTime;

/**
 * Mapper for GitHub API responses to entities.
 * Handles conversion from GitHub API DTOs to database entities.
 */
public final class GitHubResponseMapper {

    private GitHubResponseMapper() {}

    /**
     * Creates or updates a PullRequestEntity from GitHub API response.
     * Sets basic PR data and BASIC data level.
     * 
     * @param response GitHub PR response
     * @param repository Repository entity to associate with
     * @param existingEntity Existing entity to update (null for new)
     * @return Updated or new PullRequestEntity
     */
    public static PullRequestEntity toPullRequestEntity(GitHubPullRequestResponse response, 
                                                       RepositoryEntity repository,
                                                       PullRequestEntity existingEntity) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }

        PullRequestEntity entity = existingEntity != null ? existingEntity : new PullRequestEntity();
        
        // Set basic PR data
        entity.setGithubPrId(response.id());
        entity.setPrNumber(response.number());
        entity.setRepository(repository);
        entity.setState(response.state());
        entity.setDraft(response.draft());
        entity.setTitle(response.title());
        entity.setBody(response.body());
        entity.setHtmlUrl(response.htmlUrl());
        entity.setMergeCommitSha(response.mergeCommitSha());
        entity.setCreatedAt(response.createdAt());
        entity.setUpdatedAt(response.updatedAt());
        entity.setClosedAt(response.closedAt());
        entity.setMergedAt(response.mergedAt());
        entity.setLastSyncedAt(OffsetDateTime.now());

        // Set author information
        if (response.user() != null) {
            entity.setAuthorLogin(response.user().login());
            entity.setAuthorGithubId(response.user().id());
        }

        // Set data level to BASIC if not already set
        if (entity.getDataLevel() == null) {
            entity.setDataLevel(DataLevel.BASIC);
        }

        return entity;
    }

    /**
     * Updates a PullRequestEntity with detailed information from GitHub API.
     * Updates change statistics and potentially data level.
     * 
     * @param entity Existing PullRequestEntity to update
     * @param response GitHub PR detail response
     * @return Updated PullRequestEntity
     */
    public static PullRequestEntity updateWithDetails(PullRequestEntity entity, 
                                                     GitHubPullRequestDetailResponse response) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }

        // Update change statistics
        entity.setAdditions(response.additions());
        entity.setDeletions(response.deletions());
        entity.setChangedFiles(response.changedFiles());
        entity.setChangeStatsSyncedAt(OffsetDateTime.now());

        // Update other fields that might have changed
        entity.setState(response.state());
        entity.setDraft(response.draft());
        entity.setTitle(response.title());
        entity.setBody(response.body());
        entity.setUpdatedAt(response.updatedAt());
        entity.setClosedAt(response.closedAt());
        entity.setMergedAt(response.mergedAt());
        entity.setLastSyncedAt(OffsetDateTime.now());

        // Update data level based on completeness
        updateDataLevel(entity);

        return entity;
    }

    /**
     * Creates or updates a PullRequestReviewEntity from GitHub API response.
     * 
     * @param response GitHub review response
     * @param pullRequest PullRequest entity to associate with
     * @param existingEntity Existing entity to update (null for new)
     * @return Updated or new PullRequestReviewEntity
     */
    public static PullRequestReviewEntity toPullRequestReviewEntity(GitHubPullRequestReviewResponse response,
                                                                   PullRequestEntity pullRequest,
                                                                   PullRequestReviewEntity existingEntity) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        if (pullRequest == null) {
            throw new IllegalArgumentException("PullRequest cannot be null");
        }

        PullRequestReviewEntity entity = existingEntity != null ? existingEntity : new PullRequestReviewEntity();

        entity.setGithubReviewId(response.id());
        entity.setPullRequest(pullRequest);
        entity.setState(response.state());
        entity.setBody(response.body());
        entity.setSubmittedAt(response.submittedAt());
        entity.setHtmlUrl(response.htmlUrl());

        // Set reviewer information
        if (response.user() != null) {
            entity.setReviewerLogin(response.user().login());
            entity.setReviewerGithubId(response.user().id());
        }

        return entity;
    }

    /**
     * Creates or updates a PullRequestCommitEntity from GitHub API response.
     * 
     * @param response GitHub commit response
     * @param pullRequest PullRequest entity to associate with
     * @param githubRepoId GitHub repository ID
     * @param existingEntity Existing entity to update (null for new)
     * @return Updated or new PullRequestCommitEntity
     */
    public static PullRequestCommitEntity toPullRequestCommitEntity(GitHubPullRequestCommitResponse response,
                                                                   PullRequestEntity pullRequest,
                                                                   Long githubRepoId,
                                                                   PullRequestCommitEntity existingEntity) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        if (pullRequest == null) {
            throw new IllegalArgumentException("PullRequest cannot be null");
        }
        if (githubRepoId == null) {
            throw new IllegalArgumentException("GitHub repo ID cannot be null");
        }

        PullRequestCommitEntity entity = existingEntity != null ? existingEntity : new PullRequestCommitEntity();

        entity.setGithubRepoId(githubRepoId);
        entity.setSha(response.sha());
        entity.setPullRequest(pullRequest);
        entity.setHtmlUrl(response.htmlUrl());

        // Set commit information
        if (response.commit() != null) {
            entity.setMessage(response.commit().message());

            if (response.commit().author() != null) {
                entity.setAuthorName(response.commit().author().name());
                entity.setAuthorEmail(response.commit().author().email());
                entity.setCommittedAt(response.commit().author().date());
            }
        }

        // Set author login from GitHub user info
        if (response.author() != null) {
            entity.setAuthorLogin(response.author().login());
        }

        return entity;
    }

    /**
     * Updates data level based on completeness of PR data.
     * Transitions from BASIC to FULL when all detail data is available.
     */
    private static void updateDataLevel(PullRequestEntity entity) {
        boolean hasChangeStats = entity.getAdditions() != null && 
                                entity.getDeletions() != null && 
                                entity.getChangedFiles() != null;

        DataLevel newDataLevel = hasChangeStats ? DataLevel.FULL : DataLevel.BASIC;

        if (entity.getDataLevel() != newDataLevel) {
            entity.setDataLevel(newDataLevel);
        }
    }
}