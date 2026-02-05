package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUserApiResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("login") String login,
        @JsonProperty("name") String name,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("type") String type
) {}
