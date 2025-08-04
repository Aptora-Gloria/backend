package com.oobeya.github.dto;

public record LanguageInitRequest(
        String owner,
        String repo
) {}