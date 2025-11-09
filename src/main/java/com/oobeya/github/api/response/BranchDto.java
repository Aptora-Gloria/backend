package com.oobeya.github.api.response;

public record BranchDto(
        String name,
        String uid,
        String repoHtmlUrl
) {}
