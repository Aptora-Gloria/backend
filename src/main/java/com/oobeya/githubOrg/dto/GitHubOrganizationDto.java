package com.oobeya.githubOrg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubOrganizationDto(
        String login,

        @JsonProperty("id")
        Long organizationId,

        @JsonProperty("avatar_url")
        String avatarUrl,

        @JsonProperty("description")
        String description
) {}
