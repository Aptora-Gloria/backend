package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.optimetric.github.application.dto.TriggerSyncResult;

/**
 * Sync tetikleme API yanıtı.
 */
public record TriggerSyncResponse(
    @JsonProperty("status")
    String status,
    @JsonProperty("message")
    String message
) {
    public static TriggerSyncResponse from(TriggerSyncResult result) {
        return new TriggerSyncResponse(
                result.status().name(),
                result.message() != null ? result.message() : ""
        );
    }
}
