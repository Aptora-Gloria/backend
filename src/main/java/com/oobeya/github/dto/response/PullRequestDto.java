package com.oobeya.github.dto.response;

import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Response DTO for pull request data.
 * Maps from PullRequestEntity to DTO following the same pattern as other modules.
 */
public record PullRequestDto(
        Long id,
        Integer number,
        String title,
        PullRequestEntity.PullRequestStatus status,
        boolean merged,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PullRequestDto fromEntity(PullRequestEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return new PullRequestDto(
                entity.getId(),
                entity.getNumber(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getStatus() == PullRequestEntity.PullRequestStatus.MERGED,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

