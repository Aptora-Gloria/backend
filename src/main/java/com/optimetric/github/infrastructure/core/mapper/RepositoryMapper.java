package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.shared.domain.OwnerType;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Mapper for converting between application DTOs and infrastructure entities.
 * This mapper is used in the infrastructure layer.
 */
public class RepositoryMapper {

    /**
     * Parses ISO date-time string to OffsetDateTime.
     * Public for use in RepositoryPersistenceAdapter.
     */
    public static OffsetDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Maps owner type string to OwnerType enum.
     * Public for use in RepositoryPersistenceAdapter.
     */
    public static OwnerType mapOwnerType(String ownerType) {
        if (ownerType == null) return null;
        return switch (ownerType.toUpperCase(Locale.ROOT)) {
            case "USER" -> OwnerType.USER;
            case "ORGANIZATION" -> OwnerType.ORGANIZATION;
            default -> null;
        };
    }

    /**
     * Maps RepositoryInitData (application) to RepositoryEntity (infrastructure).
     */
    public static RepositoryEntity toEntity(RepositoryInitData data) {
        var ownerLogin = data.owner() != null ? data.owner().login() : null;
        var ownerType = data.owner() != null ? mapOwnerType(data.owner().type()) : null;
        return RepositoryEntity.builder()
                .githubRepoId(data.id())
                .name(data.name())
                .fullName(data.fullName())
                .description(data.description())
                .htmlUrl(data.htmlUrl())
                .isPrivate(data.isPrivate())
                .isFork(data.fork())
                .stargazersCount(data.stargazersCount())
                .watchersCount(data.watchersCount())
                .forksCount(data.forksCount())
                .openIssuesCount(data.openIssuesCount())
                .size(data.size())
                .language(data.language())
                .defaultBranch(data.defaultBranch())
                .createdAt(parseTime(data.createdAt()))
                .updatedAt(parseTime(data.updatedAt()))
                .pushedAt(parseTime(data.pushedAt()))
                .ownerLogin(ownerLogin)
                .ownerType(ownerType)
                .lastSyncedAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Maps RepositoryEntity (infrastructure) to RepositoryInfo (application).
     */
    public static RepositoryInfo toRepositoryInfo(RepositoryEntity entity) {
        if (entity == null) return null;
        return new RepositoryInfo(
                entity.getId(),
                entity.getGithubRepoId(),
                entity.getName(),
                entity.getFullName(),
                entity.getDescription(),
                entity.getHtmlUrl(),
                entity.getIsPrivate(),
                entity.getIsFork(),
                entity.getStargazersCount(),
                entity.getWatchersCount(),
                entity.getForksCount(),
                entity.getOpenIssuesCount(),
                entity.getSize(),
                entity.getLanguage(),
                entity.getDefaultBranch(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPushedAt(),
                entity.getOwnerLogin(),
                entity.getOwnerType(),
                entity.getLastSyncedAt()
        );
    }

    /**
     * Maps list of RepositoryInitData to list of RepositoryEntity.
     */
    public static List<RepositoryEntity> toEntities(List<RepositoryInitData> dataList) {
        return dataList.stream()
                .map(RepositoryMapper::toEntity)
                .toList();
    }

    /**
     * Maps list of RepositoryEntity to list of RepositoryInfo.
     */
    public static List<RepositoryInfo> toRepositoryInfos(List<RepositoryEntity> entities) {
        return entities.stream()
                .map(RepositoryMapper::toRepositoryInfo)
                .collect(Collectors.toList());
    }
}

