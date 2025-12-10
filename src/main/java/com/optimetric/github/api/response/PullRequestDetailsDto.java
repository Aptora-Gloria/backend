package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * DTO for GitHub Pull Request Details API response.
 * This contains additional metrics not available in the list endpoint.
 */
public record PullRequestDetailsDto(
        @JsonProperty("id") Long githubPrId,
        @JsonProperty("number") Integer number,
        @JsonProperty("additions") Integer additions,
        @JsonProperty("deletions") Integer deletions,
        @JsonProperty("changed_files") Integer changedFiles,
        @JsonProperty("merged") Boolean merged,
        @JsonProperty("merged_at") OffsetDateTime mergedAt
) {}

