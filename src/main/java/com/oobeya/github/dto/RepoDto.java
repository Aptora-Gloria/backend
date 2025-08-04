package com.oobeya.github.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RepoDto(
        String name,
        String fullName,
        @JsonProperty("html_url") String htmlUrl,
        String description,
        boolean fork
) {}

