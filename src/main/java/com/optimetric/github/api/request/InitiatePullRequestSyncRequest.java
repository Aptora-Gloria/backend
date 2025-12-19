package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for initiating Pull Request synchronization.
 */
public record InitiatePullRequestSyncRequest(
        @NotBlank(message = "repoFullName is required")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$", 
                message = "repoFullName must be in format 'owner/repo' with valid characters")
        String repoFullName
) {}