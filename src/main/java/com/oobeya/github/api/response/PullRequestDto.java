package com.oobeya.github.api.response;

import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;

import java.time.OffsetDateTime;

public record PullRequestDto(
        Long id,
        Integer number,
        String title,
        PullRequestEntity.PullRequestStatus status,
        boolean merged,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

