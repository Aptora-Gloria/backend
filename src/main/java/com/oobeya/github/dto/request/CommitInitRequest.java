package com.oobeya.github.dto.request;

public record CommitInitRequest(
        String owner,
        String repo
) {}
