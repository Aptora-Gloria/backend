package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record GetPullRequestRequest(
        @NotBlank(message = "repoFullName is required")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$", 
                message = "repoFullName must be in format 'owner/repo' with valid characters")
        String repoFullName,
        
        @NotNull(message = "prNumber is required")
        @Positive(message = "prNumber must be positive")
        Integer prNumber
) {}