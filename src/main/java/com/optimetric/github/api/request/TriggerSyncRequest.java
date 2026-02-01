package com.optimetric.github.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Sync tetikleme isteği: repo fullName.
 */
public record TriggerSyncRequest(
    @NotBlank(message = "Repository full name is required")
    String fullName
) {
}
