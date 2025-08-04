package com.oobeya.github.dto.request;

public record LanguageInitRequest(
        String owner,
        String repo
) {}