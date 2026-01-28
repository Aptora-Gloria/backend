package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Nested DTO for repository readiness flags.
 * Each flag indicates whether a specific data component is available.
 */
public record RepositoryReadinessFlags(
    @JsonProperty("branchesReady")
    boolean branchesReady,
    
    @JsonProperty("commitsReady")
    boolean commitsReady,
    
    @JsonProperty("pullRequestsReady")
    boolean pullRequestsReady,
    
    @JsonProperty("prDetailsReady")
    boolean prDetailsReady,
    
    @JsonProperty("reviewsReady")
    boolean reviewsReady,
    
    @JsonProperty("languagesReady")
    boolean languagesReady
) {
}
