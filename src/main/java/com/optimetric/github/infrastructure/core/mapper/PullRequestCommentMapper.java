package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.PullRequestCommentDto;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommentEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class PullRequestCommentMapper {

    private PullRequestCommentMapper() {}

    public static PullRequestCommentEntity toEntity(PullRequestCommentDto dto, PullRequestEntity pullRequest) {
        if (dto == null || pullRequest == null) {
            throw new IllegalArgumentException("DTO and pull request cannot be null");
        }

        return PullRequestCommentEntity.builder()
                .githubCommentId(dto.githubCommentId())
                .authorGithubId(dto.author() != null ? dto.author().githubId() : null)
                .authorLogin(dto.author() != null ? dto.author().login() : null)
                .body(dto.body())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .pullRequest(pullRequest)
                .build();
    }

    public static PullRequestCommentDto toDto(PullRequestCommentEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PullRequestCommentDto(
                entity.getGithubCommentId(),
                entity.getBody(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                new PullRequestCommentDto.AuthorInfo(entity.getAuthorGithubId(), entity.getAuthorLogin())
        );
    }

    public static List<PullRequestCommentEntity> toEntities(
            List<PullRequestCommentDto> dtos,
            PullRequestEntity pullRequest
    ) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, pullRequest))
                .collect(Collectors.toList());
    }

    public static List<PullRequestCommentDto> toDtos(List<PullRequestCommentEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestCommentMapper::toDto)
                .collect(Collectors.toList());
    }
}

