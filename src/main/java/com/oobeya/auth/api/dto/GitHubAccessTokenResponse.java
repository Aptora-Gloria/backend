package com.oobeya.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub access token yanıtını karşılar.
 */
public record GitHubAccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String scope
) {}
