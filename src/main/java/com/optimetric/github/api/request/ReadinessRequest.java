package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Hazırlık sorgulama isteği: repo fullName.
 */
public record ReadinessRequest(
    @NotBlank(message = "Repository full name is required")
    String fullName
) {
}
