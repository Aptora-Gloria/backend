package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

public record LanguageInitRequest(
        @NotBlank(message = "repoFullName is required")
        String repoFullName
) {}
