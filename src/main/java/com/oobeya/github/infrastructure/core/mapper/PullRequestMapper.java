package com.oobeya.github.infrastructure.core.mapper;

import com.oobeya.github.api.response.GitHubPullRequestResponse;
import com.oobeya.github.dto.response.PullRequestDto;
import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between PullRequestEntity, PullRequestDto, and GitHubPullRequestResponse.
 */
public class PullRequestMapper {

    public static OffsetDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    public static PullRequestEntity.PullRequestStatus mapStatus(String state, Boolean merged) {
        if (merged != null && merged) {
            return PullRequestEntity.PullRequestStatus.MERGED;
        }
        if (state == null) {
            return PullRequestEntity.PullRequestStatus.OPEN;
        }
        return switch (state.toUpperCase()) {
            case "OPEN" -> PullRequestEntity.PullRequestStatus.OPEN;
            case "CLOSED" -> PullRequestEntity.PullRequestStatus.CLOSED;
            default -> PullRequestEntity.PullRequestStatus.OPEN;
        };
    }

    /**
     * Converts GitHub API response to PullRequestDto.
     */
    public static PullRequestDto toDto(GitHubPullRequestResponse response) {
        if (response == null) return null;
        
        return new PullRequestDto(
                response.id(),
                response.number(),
                response.title(),
                mapStatus(response.state(), response.merged()).equals(PullRequestEntity.PullRequestStatus.MERGED) 
                    ? PullRequestEntity.PullRequestStatus.MERGED 
                    : mapStatus(response.state(), response.merged()),
                response.merged() != null && response.merged(),
                parseTime(response.createdAt()),
                parseTime(response.updatedAt())
        );
    }

    /**
     * Converts PullRequestEntity to PullRequestDto.
     */
    public static PullRequestDto toDto(PullRequestEntity entity) {
        if (entity == null) return null;
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

    /**
     * Converts GitHub API response to PullRequestEntity.
     * Note: Requires repository and branches to be set separately.
     */
    public static PullRequestEntity toEntity(
            GitHubPullRequestResponse response,
            RepositoryEntity repository,
            BranchEntity sourceBranch,
            BranchEntity targetBranch
    ) {
        if (response == null) return null;

        return PullRequestEntity.builder()
                .githubPrId(response.id())
                .number(response.number())
                .title(response.title())
                .body(response.body())
                .status(mapStatus(response.state(), response.merged()))
                .sourceBranch(sourceBranch)
                .targetBranch(targetBranch)
                .repository(repository)
                .htmlUrl(response.htmlUrl())
                .apiUrl(response.url())
                .createdAt(parseTime(response.createdAt()))
                .updatedAt(parseTime(response.updatedAt()))
                .closedAt(parseTime(response.closedAt()))
                .mergedAt(parseTime(response.mergedAt()))
                .build();
    }

    /**
     * Converts list of GitHub API responses to DTOs.
     */
    public static List<PullRequestDto> toDtos(List<GitHubPullRequestResponse> responses) {
        if (responses == null) return List.of();
        return responses.stream()
                .map(PullRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of entities to DTOs.
     */
    public static List<PullRequestDto> entitiesToDtos(List<PullRequestEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(PullRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}

