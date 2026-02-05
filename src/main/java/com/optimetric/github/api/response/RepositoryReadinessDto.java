package com.optimetric.github.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.optimetric.github.domain.enumeration.MissingComponent;
import com.optimetric.github.domain.enumeration.ReadinessState;

public record RepositoryReadinessDto(
    @JsonProperty("state")
    ReadinessState state,
    
    @JsonProperty("missing")
    List<MissingComponent> missing,
    
    @JsonProperty("flags")
    RepositoryReadinessFlags flags
) {
}
