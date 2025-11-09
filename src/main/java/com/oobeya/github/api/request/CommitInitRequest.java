package com.oobeya.github.api.request;

public record CommitInitRequest(
        Long repositoryId,
        Long branchId
) {}
