package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record PullRequestCommitFromApi(
        String sha,
        String message,
        String htmlUrl,
        String authorLogin,
        String authorName,
        OffsetDateTime committedAt
) {}
