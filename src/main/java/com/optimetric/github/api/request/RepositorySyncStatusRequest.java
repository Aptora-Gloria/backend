package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for repository sync status endpoint.
 */
public record RepositorySyncStatusRequest(
    @NotBlank(message = "Repository full name is required")
    String fullName
) {
}
