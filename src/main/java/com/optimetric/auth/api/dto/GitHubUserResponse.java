package com.optimetric.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUserResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("login")
        String login,

        @JsonProperty("name")
        String name,

        @JsonProperty("avatar_url")
        String avatar_url,

        @JsonProperty("html_url")
        String htmlUrl,

        @JsonProperty("type")
        String type
) {}