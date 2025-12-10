package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.util.List;

public final class PullRequestMapper {

    private PullRequestMapper() {}

    public static PullRequestDto toDto(PullRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PullRequestDto(
                entity.getGithubPrId(),
                entity.getNumber(),
                entity.getTitle(),
                entity.getBody(),
                entity.getState(),
                entity.isLocked(),
                entity.isDraft(),
                entity.isMerged(),
                entity.getMergeCommitSha(),
                entity.getHtmlUrl(),
                entity.getDiffUrl(),
                entity.getPatchUrl(),
                new PullRequestDto.AuthorInfo(
                        entity.getAuthorGithubId(),
                        entity.getAuthorLogin()
                ),
                new PullRequestDto.BranchInfo(
                        entity.getHeadRef(),
                        entity.getHeadSha()
                ),
                new PullRequestDto.BranchInfo(
                        entity.getBaseRef(),
                        entity.getBaseSha()
                ),
                entity.getCommitCount(),
                entity.getAdditions(),
                entity.getDeletions(),
                entity.getChangedFiles(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getClosedAt(),
                entity.getMergedAt()
        );
    }

    public static List<PullRequestDto> toDtos(List<PullRequestEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestMapper::toDto)
                .toList();
    }

    public static PullRequestEntity toEntity(PullRequestDto dto, RepositoryEntity repository) {
        if (dto == null || repository == null) {
            throw new IllegalArgumentException("DTO and repository cannot be null");
        }

        return PullRequestEntity.builder()
                .githubPrId(dto.githubPrId())
                .number(dto.number())
                .title(dto.title())
                .body(dto.body())
                .state(dto.state())
                .locked(dto.locked() != null && dto.locked())
                .draft(dto.draft() != null && dto.draft())
                .merged(dto.merged() != null && dto.merged())
                .mergeCommitSha(dto.mergeCommitSha())
                .htmlUrl(dto.htmlUrl())
                .diffUrl(dto.diffUrl())
                .patchUrl(dto.patchUrl())
                .authorGithubId(dto.author() != null ? dto.author().githubId() : null)
                .authorLogin(dto.author() != null ? dto.author().login() : null)
                .headRef(dto.head() != null ? dto.head().ref() : null)
                .headSha(dto.head() != null ? dto.head().sha() : null)
                .baseRef(dto.base() != null ? dto.base().ref() : null)
                .baseSha(dto.base() != null ? dto.base().sha() : null)
                .commitCount(dto.commitCount())
                .additions(dto.additions())
                .deletions(dto.deletions())
                .changedFiles(dto.changedFiles())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .closedAt(dto.closedAt())
                .mergedAt(dto.mergedAt())
                .repository(repository)
                .build();
    }

    public static List<PullRequestEntity> toEntities(List<PullRequestDto> dtos, RepositoryEntity repository) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, repository))
                .toList();
    }
}


