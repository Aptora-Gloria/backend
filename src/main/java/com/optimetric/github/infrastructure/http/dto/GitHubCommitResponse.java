package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GitHubCommitResponse(
        @JsonProperty("sha") String sha,
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("commit") Commit commit,
        @JsonProperty("author") Author author,
        @JsonProperty("committer") Committer committer,
        @JsonProperty("parents") List<Parent> parents,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("url") String apiUrl
) {
    public record Commit(
            String message,
            @JsonProperty("author") CommitAuthor author,
            @JsonProperty("committer") CommitCommitter committer
    ) {}
    public record CommitAuthor(String name, String email, String date) {}
    public record CommitCommitter(String name, String email, String date) {}
    public record Author(@JsonProperty("login") String login) {}
    public record Committer(@JsonProperty("login") String login) {}
    public record Parent(@JsonProperty("sha") String sha) {}
}
