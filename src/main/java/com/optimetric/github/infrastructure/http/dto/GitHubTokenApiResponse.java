package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubTokenApiResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String scope
) {}
