package com.oobeya.github.dto;

public record CommitInitRequest(
        String owner,
        String repo
) {}
