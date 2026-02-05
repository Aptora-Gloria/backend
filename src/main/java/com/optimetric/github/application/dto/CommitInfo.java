package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CommitInfo(
        String sha,
        String nodeId,
        String message,
        String authorLogin,
        String committerLogin,
        List<String> parentShas,
        String htmlUrl,
        String apiUrl,
        OffsetDateTime committedAt,
        Boolean mergeCommit
) {}
