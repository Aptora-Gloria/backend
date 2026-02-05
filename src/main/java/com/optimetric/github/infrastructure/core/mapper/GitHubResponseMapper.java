package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.PullRequestCommitFromApi;
import com.optimetric.github.application.dto.PullRequestDetailFromApi;
import com.optimetric.github.application.dto.PullRequestFromApi;
import com.optimetric.github.application.dto.PullRequestReviewFromApi;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.time.OffsetDateTime;

public final class GitHubResponseMapper {

    private GitHubResponseMapper() {}

    public static PullRequestEntity toPullRequestEntity(PullRequestFromApi response,
                                                       RepositoryEntity repository,
                                                       PullRequestEntity existingEntity) {
        if (response == null) throw new IllegalArgumentException("Response cannot be null");
        if (repository == null) throw new IllegalArgumentException("Repository cannot be null");

        PullRequestEntity entity = existingEntity != null ? existingEntity : new PullRequestEntity();
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
        if (response.user() != null) {
            entity.setAuthorLogin(response.user().login());
            entity.setAuthorGithubId(response.user().id());
        }
        if (entity.getDataLevel() == null) entity.setDataLevel(DataLevel.BASIC);
        return entity;
    }

    public static PullRequestEntity updateWithDetails(PullRequestEntity entity, PullRequestDetailFromApi response) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        if (response == null) throw new IllegalArgumentException("Response cannot be null");
        entity.setAdditions(response.additions());
        entity.setDeletions(response.deletions());
        entity.setChangedFiles(response.changedFiles());
        entity.setChangeStatsSyncedAt(OffsetDateTime.now());
        entity.setState(response.state());
        entity.setDraft(response.draft());
        entity.setTitle(response.title());
        entity.setBody(response.body());
        entity.setUpdatedAt(response.updatedAt());
        entity.setClosedAt(response.closedAt());
        entity.setMergedAt(response.mergedAt());
        entity.setLastSyncedAt(OffsetDateTime.now());
        updateDataLevel(entity);
        return entity;
    }

    public static PullRequestReviewEntity toPullRequestReviewEntity(PullRequestReviewFromApi response,
                                                                   PullRequestEntity pullRequest,
                                                                   PullRequestReviewEntity existingEntity) {
        if (response == null) throw new IllegalArgumentException("Response cannot be null");
        if (pullRequest == null) throw new IllegalArgumentException("PullRequest cannot be null");
        PullRequestReviewEntity entity = existingEntity != null ? existingEntity : new PullRequestReviewEntity();
        entity.setGithubReviewId(response.id());
        entity.setPullRequest(pullRequest);
        entity.setState(response.state());
        entity.setBody(response.body());
        entity.setSubmittedAt(response.submittedAt());
        entity.setHtmlUrl(response.htmlUrl());
        if (response.user() != null) {
            entity.setReviewerLogin(response.user().login());
            entity.setReviewerGithubId(response.user().id());
        }
        return entity;
    }

    public static PullRequestCommitEntity toPullRequestCommitEntity(PullRequestCommitFromApi response,
                                                                   PullRequestEntity pullRequest,
                                                                   Long githubRepoId,
                                                                   PullRequestCommitEntity existingEntity) {
        if (response == null) throw new IllegalArgumentException("Response cannot be null");
        if (pullRequest == null) throw new IllegalArgumentException("PullRequest cannot be null");
        if (githubRepoId == null) throw new IllegalArgumentException("GitHub repo ID cannot be null");
        PullRequestCommitEntity entity = existingEntity != null ? existingEntity : new PullRequestCommitEntity();
        entity.setGithubRepoId(githubRepoId);
        entity.setSha(response.sha());
        entity.setPullRequest(pullRequest);
        entity.setMessage(response.message());
        entity.setHtmlUrl(response.htmlUrl());
        entity.setAuthorName(response.authorName());
        entity.setAuthorLogin(response.authorLogin());
        entity.setCommittedAt(response.committedAt());
        return entity;
    }

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