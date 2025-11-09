package com.oobeya.github.api.request;

public record CommitInitRequest(
        String owner,
        String repo
) {}
