package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.CommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class CommitMapper {

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private CommitMapper() {}

    /**
     * Maps CommitDto from GitHub API to CommitEntity.
     * 
     * @param dto The GitHub API commit DTO
     * @param repository The repository entity
     * @param branch The branch entity
     * @return CommitEntity
     */
    public static CommitEntity toEntity(CommitDto dto, RepositoryEntity repository, BranchEntity branch) {
        if (dto == null || repository == null || branch == null) {
            throw new IllegalArgumentException("DTO, repository, and branch cannot be null");
        }

        CommitDto.CommitInfo commitInfo = dto.commit();
        boolean isMergeCommit = dto.parents() != null && dto.parents().size() > 1;

        return CommitEntity.builder()
                .sha(dto.sha())
                .nodeId(dto.nodeId())
                .message(commitInfo != null ? commitInfo.message() : null)
                // Author info
                .authorName(commitInfo != null && commitInfo.author() != null ? commitInfo.author().name() : null)
                .authorEmail(commitInfo != null && commitInfo.author() != null ? commitInfo.author().email() : null)
                .authoredAt(parseDateTime(commitInfo != null && commitInfo.author() != null ? commitInfo.author().date() : null))
                // Committer info
                .committerName(commitInfo != null && commitInfo.committer() != null ? commitInfo.committer().name() : null)
                .committerEmail(commitInfo != null && commitInfo.committer() != null ? commitInfo.committer().email() : null)
                .committedAt(parseDateTime(commitInfo != null && commitInfo.committer() != null ? commitInfo.committer().date() : null))
                // GitHub user logins
                .authorLogin(dto.author() != null ? dto.author().login() : null)
                .committerLogin(dto.committer() != null ? dto.committer().login() : null)
                // Merge commit flag
                .mergeCommit(isMergeCommit)
                // URLs
                .htmlUrl(dto.htmlUrl())
                .apiUrl(dto.apiUrl())
                // Relationships
                .repository(repository)
                .branch(branch)
                .build();
    }

    /**
     * Maps CommitEntity to CommitDto for API response.
     * 
     * @param entity The commit entity
     * @return CommitDto
     */
    public static CommitDto toDto(CommitEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        return new CommitDto(
                entity.getSha(),
                entity.getNodeId(),
                new CommitDto.CommitInfo(
                        entity.getMessage(),
                        new CommitDto.CommitInfo.CommitAuthor(
                                entity.getAuthorName(),
                                entity.getAuthorEmail(),
                                formatDateTime(entity.getAuthoredAt())
                        ),
                        new CommitDto.CommitInfo.CommitCommitter(
                                entity.getCommitterName(),
                                entity.getCommitterEmail(),
                                formatDateTime(entity.getCommittedAt())
                        )
                ),
                new CommitDto.AuthorInfo(entity.getAuthorLogin()),
                new CommitDto.CommitterInfo(entity.getCommitterLogin()),
                null, // Parents not stored in entity
                entity.getHtmlUrl(),
                entity.getApiUrl()
        );
    }

    /**
     * Maps list of CommitDto to list of CommitEntity.
     * 
     * @param dtos List of commit DTOs
     * @param repository The repository entity
     * @param branch The branch entity
     * @return List of CommitEntity
     */
    public static List<CommitEntity> toEntities(
            List<CommitDto> dtos,
            RepositoryEntity repository,
            BranchEntity branch
    ) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(dto -> toEntity(dto, repository, branch))
                .collect(Collectors.toList());
    }

    /**
     * Maps list of CommitEntity to list of CommitDto.
     * 
     * @param entities List of commit entities
     * @return List of CommitDto
     */
    public static List<CommitDto> toDtos(List<CommitEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps CommitEntity to application CommitInfo.
     */
    public static CommitInfo toCommitInfo(CommitEntity entity) {
        if (entity == null) return null;
        return new CommitInfo(
                entity.getSha(),
                entity.getNodeId(),
                entity.getMessage(),
                entity.getAuthorLogin(),
                entity.getCommitterLogin(),
                null, // parentShas not stored in entity
                entity.getHtmlUrl(),
                entity.getApiUrl()
        );
    }

    public static List<CommitInfo> toCommitInfos(List<CommitEntity> entities) {
        return entities == null ? List.of() : entities.stream().map(CommitMapper::toCommitInfo).collect(Collectors.toList());
    }

    /**
     * Maps API CommitDto to application CommitInfo.
     */
    public static CommitInfo fromApiDto(CommitDto dto) {
        if (dto == null) return null;
        List<String> parentShas = dto.parents() == null ? List.of() : dto.parents().stream()
                .map(p -> p != null ? p.sha() : null)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        String message = dto.commit() != null ? dto.commit().message() : null;
        String authorLogin = dto.author() != null ? dto.author().login() : null;
        String committerLogin = dto.committer() != null ? dto.committer().login() : null;
        return new CommitInfo(
                dto.sha(),
                dto.nodeId(),
                message,
                authorLogin,
                committerLogin,
                parentShas,
                dto.htmlUrl(),
                dto.apiUrl()
        );
    }

    public static List<CommitInfo> fromApiDtos(List<CommitDto> dtos) {
        return dtos == null ? List.of() : dtos.stream().map(CommitMapper::fromApiDto).collect(Collectors.toList());
    }

    /**
     * Parses ISO 8601 date-time string to OffsetDateTime.
     */
    private static OffsetDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateTimeStr, ISO_DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formats OffsetDateTime to ISO 8601 string.
     */
    private static String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_DATE_TIME_FORMATTER);
    }
}
