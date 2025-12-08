package com.oobeya.github.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

/**
 * Request DTO for synchronizing pull requests from GitHub.
 */
public record PullRequestSyncRequest(
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

