package com.oobeya.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommitDto(
        @JsonProperty("sha") String sha,
        @JsonProperty("commit") CommitInfo commit
) {
    public record CommitInfo(
            String message,
            Author author
    ) {
        public record Author(String name, String date) {}
    }
}
