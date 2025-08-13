package com.oobeya.github.dto.response;

public record BranchDto(
        String name,
        String uid,
        String repoHtmlUrl
) {}
