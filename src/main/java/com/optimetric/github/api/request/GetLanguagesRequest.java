package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

public record GetLanguagesRequest(
        @NotBlank(message = "repoFullName is required")
        String repoFullName
) {}

