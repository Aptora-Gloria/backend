package com.oobeya.github.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContentDto(
        String content,
        String name,
        String path,
        String sha,
        String type,
        String encoding,
        @JsonProperty("download_url") String downloadUrl
) {}
