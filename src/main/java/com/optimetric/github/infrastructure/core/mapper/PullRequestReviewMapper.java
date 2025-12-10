package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.PullRequestReviewDto;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class PullRequestReviewMapper {

    private PullRequestReviewMapper() {}

    public static PullRequestReviewEntity toEntity(PullRequestReviewDto dto, PullRequestEntity pullRequest) {
        if (dto == null || pullRequest == null) {
            throw new IllegalArgumentException("DTO and pull request cannot be null");
        }

        return PullRequestReviewEntity.builder()
                .githubReviewId(dto.githubReviewId())
                .state(dto.state())
                .authorGithubId(dto.author() != null ? dto.author().githubId() : null)
                .authorLogin(dto.author() != null ? dto.author().login() : null)
                .body(dto.body())
                .submittedAt(dto.submittedAt())
                .pullRequest(pullRequest)
                .build();
    }

    public static PullRequestReviewDto toDto(PullRequestReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PullRequestReviewDto(
                entity.getGithubReviewId(),
                entity.getState(),
                entity.getBody(),
                entity.getSubmittedAt(),
                new PullRequestReviewDto.AuthorInfo(entity.getAuthorGithubId(), entity.getAuthorLogin())
        );
    }

    public static List<PullRequestReviewEntity> toEntities(
            List<PullRequestReviewDto> dtos,
            PullRequestEntity pullRequest
    ) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, pullRequest))
                .collect(Collectors.toList());
    }

    public static List<PullRequestReviewDto> toDtos(List<PullRequestReviewEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestReviewMapper::toDto)
                .collect(Collectors.toList());
    }
}

