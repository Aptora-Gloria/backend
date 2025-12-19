package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CommitDto(
        @JsonProperty("sha") String sha,
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("commit") CommitInfo commit,
        @JsonProperty("author") AuthorInfo author,
        @JsonProperty("committer") CommitterInfo committer,
        @JsonProperty("parents") List<ParentInfo> parents,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("url") String apiUrl
) {
    public record CommitInfo(
            String message,
            @JsonProperty("author") CommitAuthor author,
            @JsonProperty("committer") CommitCommitter committer
    ) {
        public record CommitAuthor(
                String name,
                String email,
                String date
        ) {}
        
        public record CommitCommitter(
                String name,
                String email,
                String date
        ) {}
    }
    
    public record AuthorInfo(
            @JsonProperty("login") String login
    ) {}
    
    public record CommitterInfo(
            @JsonProperty("login") String login
    ) {}
    
    public record ParentInfo(
            @JsonProperty("sha") String sha
    ) {}
}
