package com.oobeya.githubOrg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrgCommitDto(
        @JsonProperty("sha") String sha,
        @JsonProperty("commit") Commit commit,
        @JsonProperty("author") Author author
) {
    public record Commit(
            @JsonProperty("message") String message,
            @JsonProperty("author") CommitAuthor author
    ) {}

    public record CommitAuthor(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("date") String date
    ) {}

    public record Author(
            @JsonProperty("login") String login,
            @JsonProperty("avatar_url") String avatarUrl
    ) {}
}
