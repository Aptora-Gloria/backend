package com.optimetric.metrics.api.request;

import jakarta.validation.constraints.NotBlank;

public record GetUserMetricsRequest(
        @NotBlank(message = "login is required")
        String login
) {}

