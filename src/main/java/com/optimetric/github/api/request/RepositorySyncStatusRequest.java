package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

public record RepositorySyncStatusRequest(
    @NotBlank(message = "Repository full name is required")
    String fullName
) {
}
