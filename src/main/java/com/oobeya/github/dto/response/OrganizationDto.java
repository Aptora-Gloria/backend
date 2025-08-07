package com.oobeya.github.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record OrganizationDto(
        String login,
        String description,
        @JsonProperty("avatar_url")
        String avatarUrl,
        String type
) {}
