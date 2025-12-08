package com.oobeya.github.domain.pullrequest;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Domain representation of a pull request that enforces immutability by
 * construction and controlled state transitions through factory methods.
 */
public final class PullRequest {

    private final Long id;
    private final Long repositoryId;
    private final Integer number;
    private final String title;
    private final String body;
    private final PullRequestState state;
    private final boolean merged;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final OffsetDateTime closedAt;
    private final OffsetDateTime mergedAt;

    private PullRequest(
            Long id,
            Long repositoryId,
            Integer number,
            String title,
            String body,
            PullRequestState state,
            boolean merged,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            OffsetDateTime mergedAt
    ) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.number = number;
        this.title = title;
        this.body = body;
        this.state = state;
        this.merged = merged;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.mergedAt = mergedAt;
    }

    public static PullRequest of(
            Long id,
            Long repositoryId,
            Integer number,
            String title,
            String body,
            PullRequestState state,
            boolean merged,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            OffsetDateTime mergedAt
    ) {
        Objects.requireNonNull(repositoryId, "repositoryId must not be null");
        Objects.requireNonNull(number, "number must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        return new PullRequest(
                id,
                repositoryId,
                number,
                title,
                body,
                state,
                merged,
                createdAt,
                updatedAt == null ? createdAt : updatedAt,
                closedAt,
                mergedAt
        );
    }

    /**
     * Returns a new instance with an updated state while preserving immutability.
     */
    public PullRequest withState(PullRequestState newState, boolean mergedFlag, OffsetDateTime newUpdatedAt) {
        Objects.requireNonNull(newState, "newState must not be null");

        return new PullRequest(
                id,
                repositoryId,
                number,
                title,
                body,
                newState,
                mergedFlag,
                createdAt,
                newUpdatedAt == null ? updatedAt : newUpdatedAt,
                mergedFlag ? (mergedAt == null ? newUpdatedAt : mergedAt) : closedAt,
                mergedFlag ? (mergedAt == null ? newUpdatedAt : mergedAt) : mergedAt
        );
    }

    public PullRequest withTitle(String newTitle, OffsetDateTime newUpdatedAt) {
        Objects.requireNonNull(newTitle, "newTitle must not be null");

        return new PullRequest(
                id,
                repositoryId,
                number,
                newTitle,
                body,
                state,
                merged,
                createdAt,
                newUpdatedAt == null ? updatedAt : newUpdatedAt,
                closedAt,
                mergedAt
        );
    }

    public PullRequest withBody(String newBody, OffsetDateTime newUpdatedAt) {
        return new PullRequest(
                id,
                repositoryId,
                number,
                title,
                newBody,
                state,
                merged,
                createdAt,
                newUpdatedAt == null ? updatedAt : newUpdatedAt,
                closedAt,
                mergedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public Integer getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public PullRequestState getState() {
        return state;
    }

    public boolean isMerged() {
        return merged;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public OffsetDateTime getMergedAt() {
        return mergedAt;
    }
}

