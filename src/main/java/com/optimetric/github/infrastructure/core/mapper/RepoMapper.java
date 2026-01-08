package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RepoMapper {
    private static OffsetDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static OwnerType mapOwnerType(String ownerType) {
        if (ownerType == null) return null;
        return switch (ownerType.toUpperCase(Locale.ROOT)) {
            case "USER" -> OwnerType.USER;
            case "ORGANIZATION" -> OwnerType.ORGANIZATION;
            default -> null;
        };
    }

    public static RepositoryEntity toEntity(RepoInitRequest dto) {
        var ownerLogin = dto.owner() != null ? dto.owner().login() : null;
        var ownerType  = dto.owner() != null ? mapOwnerType(dto.owner().type()) : null;
        return RepositoryEntity.builder()
                .githubRepoId(dto.id())
                .name(dto.name())
                .fullName(dto.full_name())
                .description(dto.description())
                .htmlUrl(dto.html_url())
                .isPrivate(dto.private_())
                .isFork(dto.fork())
                .stargazersCount(dto.stargazers_count())
                .watchersCount(dto.watchers_count())
                .forksCount(dto.forks_count())
                .openIssuesCount(dto.open_issues_count())
                .size(dto.size())
                .language(dto.language())
                .defaultBranch(dto.default_branch())
                .createdAt(parseTime(dto.created_at()))
                .updatedAt(parseTime(dto.updated_at()))
                .pushedAt(parseTime(dto.pushed_at()))
                .ownerLogin(ownerLogin)
                .ownerType(ownerType)
                .lastSyncedAt(OffsetDateTime.now())
                .build();


    }

    public static RepoDto toDto(RepositoryEntity e) {
        if (e == null) return null;
        // Convert shared domain OwnerType to API OwnerType
        com.optimetric.auth.infrastructure.persistence.entity.OwnerType apiOwnerType = null;
        if (e.getOwnerType() != null) {
            apiOwnerType = com.optimetric.auth.infrastructure.persistence.entity.OwnerType.valueOf(e.getOwnerType().name());
        }
        return new RepoDto(
                e.getId(),
                e.getGithubRepoId(),
                e.getName(),
                e.getFullName(),
                e.getDescription(),
                e.getHtmlUrl(),
                e.getIsPrivate(),
                e.getIsFork(),
                e.getStargazersCount(),
                e.getWatchersCount(),
                e.getForksCount(),
                e.getOpenIssuesCount(),
                e.getSize(),
                e.getLanguage(),
                e.getDefaultBranch(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getPushedAt(),
                e.getOwnerLogin(),
                apiOwnerType,
                e.getLastSyncedAt()
        );
    }

    public static List<RepositoryEntity> toEntities(List<RepoInitRequest> list) {
        return list.stream()
                .map(RepoMapper::toEntity)
                .toList();
    }

    public static List<RepoDto> toDtos(List<RepositoryEntity> list) {
        return list.stream()
                .map(RepoMapper::toDto)
                .collect(Collectors.toList());
    }
}
