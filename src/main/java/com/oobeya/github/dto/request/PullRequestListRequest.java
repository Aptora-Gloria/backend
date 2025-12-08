package com.oobeya.github.dto.request;

import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for listing pull requests.
 */
public record PullRequestListRequest(
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

