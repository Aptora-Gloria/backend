package com.optimetric.metrics.api.request;

import jakarta.validation.constraints.NotBlank;

public record GetRepositoryMetricsRequest(
        @NotBlank(message = "repoFullName is required")
        String repoFullName
) {}

