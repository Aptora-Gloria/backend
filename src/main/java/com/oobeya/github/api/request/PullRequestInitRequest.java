package com.oobeya.github.api.request;

import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

public record PullRequestInitRequest(
        Long repositoryId,      // Bizim sistemdeki RepositoryEntity.id
        Long id,                // GitHub PR id
        String node_id,         // GitHub node_id
        Integer number,         // PR number (#1, #2 ...)
        String state,           // "open", "closed" vs.
        Boolean merged,         // PR merge edilmiş mi
        Boolean draft,          // Draft PR mi
        String title,
        String body,
        String html_url,
        String api_url,
        String created_at,      // ISO-8601 string (GitHub'tan direkt)
        String updated_at,
        String closed_at,
        String merged_at,
        User user,              // PR'ı açan GitHub user
        BranchRef head,         // source branch (PR head)
        BranchRef base          // target branch (PR base)
) {

    public record User(
            String login,
            String node_id,
            String type
    ) {}

    public record BranchRef(
            String label,   // owner:branch
            String ref,     // sadece branch adı (örn: feature/login)
            String sha      // head commit sha
    ) {}

    public static record ListInitRequest(
            @NotNull(message = "repositoryId is required")
            Long repositoryId,
            @PositiveOrZero(message = "page must be zero or positive")
            Integer page,
            @Positive(message = "size must be greater than zero")
            Integer size,
            PullRequestEntity.PullRequestStatus status,
            Boolean merged
    ) {
    }

    public static record SyncInitRequest(
            @NotNull(message = "repositoryId is required")
            Long repositoryId,
            @NotBlank(message = "owner is required")
            String owner,
            @NotBlank(message = "repositoryName is required")
            String repositoryName,
            @PastOrPresent(message = "syncSince cannot be in the future")
            OffsetDateTime syncSince
    ) {
    }
}