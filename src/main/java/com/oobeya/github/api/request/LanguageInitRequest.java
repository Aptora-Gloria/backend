package com.oobeya.github.api.request;

public record LanguageInitRequest(
        String owner,
        String repo
) {}