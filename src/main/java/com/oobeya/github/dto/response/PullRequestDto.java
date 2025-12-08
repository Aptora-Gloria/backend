package com.oobeya.github.dto.response;

import com.oobeya.github.domain.pullrequest.PullRequest;
import com.oobeya.github.domain.pullrequest.PullRequestState;

import java.time.OffsetDateTime;
import java.util.Objects;

public record PullRequestDto(
        Long id,
        Integer number,
        String title,
        PullRequestState state,
        boolean merged,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PullRequestDto fromDomain(PullRequest pullRequest) {
        Objects.requireNonNull(pullRequest, "pullRequest must not be null");
        return new PullRequestDto(
                pullRequest.getId(),
                pullRequest.getNumber(),
                pullRequest.getTitle(),
                pullRequest.getState(),
                pullRequest.isMerged(),
                pullRequest.getCreatedAt(),
                pullRequest.getUpdatedAt()
        );
    }
}

