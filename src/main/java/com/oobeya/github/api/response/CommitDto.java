package com.oobeya.github.api.response;

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
