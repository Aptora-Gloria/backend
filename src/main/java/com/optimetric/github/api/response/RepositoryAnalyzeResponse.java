package com.optimetric.github.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.optimetric.github.domain.enumeration.AnalyzeState;
import com.optimetric.github.domain.enumeration.EnqueuedJobType;

/**
 * Response DTO for repository analyze endpoint.
 * Contains the analysis result, enqueued jobs, and status endpoint URL.
 */
public record RepositoryAnalyzeResponse(
    @JsonProperty("state")
    AnalyzeState state,
    
    @JsonProperty("enqueued")
    List<EnqueuedJobType> enqueued,
    
    @JsonProperty("statusEndpoint")
    String statusEndpoint
) {
}
