package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record PullRequestDto(
        @JsonProperty("id") Long githubPrId,
        @JsonProperty("number") Integer number,
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("state") String state,
        @JsonProperty("locked") Boolean locked,
        @JsonProperty("draft") Boolean draft,
        @JsonProperty("merged") Boolean merged,
        @JsonProperty("merge_commit_sha") String mergeCommitSha,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("diff_url") String diffUrl,
        @JsonProperty("patch_url") String patchUrl,
        @JsonProperty("user") AuthorInfo author,
        @JsonProperty("head") BranchInfo head,
        @JsonProperty("base") BranchInfo base,
        @JsonProperty("commits") Integer commitCount,
        @JsonProperty("additions") Integer additions,
        @JsonProperty("deletions") Integer deletions,
        @JsonProperty("changed_files") Integer changedFiles,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("closed_at") OffsetDateTime closedAt,
        @JsonProperty("merged_at") OffsetDateTime mergedAt
) {
    public record AuthorInfo(
            @JsonProperty("id") Long githubId,
            @JsonProperty("login") String login
    ) {}

    public record BranchInfo(
            @JsonProperty("ref") String ref,
            @JsonProperty("sha") String sha
    ) {}
}


