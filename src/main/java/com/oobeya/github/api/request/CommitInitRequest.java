package com.oobeya.github.api.request;

public record CommitInitRequest(
        String fullName,
        String branchName,
        String since
) {}
