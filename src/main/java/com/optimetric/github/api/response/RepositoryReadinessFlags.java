package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

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
