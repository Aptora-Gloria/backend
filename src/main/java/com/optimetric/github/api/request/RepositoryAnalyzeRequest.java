package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for repository analyze endpoint.
 */
public record RepositoryAnalyzeRequest(
    @NotBlank(message = "Repository full name is required")
    String fullName
) {
}
