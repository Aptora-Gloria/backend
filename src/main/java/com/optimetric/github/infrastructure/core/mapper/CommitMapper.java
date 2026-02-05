package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.infrastructure.http.dto.GitHubCommitResponse;
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

    public static CommitInfo fromGitHubResponse(GitHubCommitResponse r) {
        if (r == null) return null;
        List<String> parentShas = r.parents() == null ? List.of() : r.parents().stream()
                .map(p -> p != null ? p.sha() : null)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        String message = r.commit() != null ? r.commit().message() : null;
        String authorLogin = r.author() != null ? r.author().login() : null;
        String committerLogin = r.committer() != null ? r.committer().login() : null;
        String committerDateStr = r.commit() != null && r.commit().committer() != null ? r.commit().committer().date() : null;
        OffsetDateTime committedAt = parseDateTime(committerDateStr);
        boolean mergeCommit = r.parents() != null && r.parents().size() > 1;
        return new CommitInfo(
                r.sha(),
                r.nodeId(),
                message,
                authorLogin,
                committerLogin,
                parentShas,
                r.htmlUrl(),
                r.apiUrl(),
                committedAt,
                mergeCommit
        );
    }

    public static List<CommitInfo> fromGitHubResponses(List<GitHubCommitResponse> list) {
        return list == null ? List.of() : list.stream().map(CommitMapper::fromGitHubResponse).collect(Collectors.toList());
    }

    public static CommitEntity toEntity(CommitInfo info, RepositoryEntity repository, BranchEntity branch) {
        if (info == null || repository == null || branch == null) {
            throw new IllegalArgumentException("Info, repository, and branch cannot be null");
        }
        return CommitEntity.builder()
                .sha(info.sha())
                .nodeId(info.nodeId())
                .message(info.message())
                .authorLogin(info.authorLogin())
                .committerLogin(info.committerLogin())
                .committedAt(info.committedAt())
                .mergeCommit(info.mergeCommit() != null ? info.mergeCommit() : false)
                .htmlUrl(info.htmlUrl())
                .apiUrl(info.apiUrl())
                .repository(repository)
                .branch(branch)
                .build();
    }

    public static List<CommitEntity> toEntities(
            List<CommitInfo> infos,
            RepositoryEntity repository,
            BranchEntity branch
    ) {
        if (infos == null || repository == null || branch == null) {
            return List.of();
        }
        return infos.stream()
                .map(info -> toEntity(info, repository, branch))
                .collect(Collectors.toList());
    }

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
                entity.getApiUrl(),
                entity.getCommittedAt(),
                entity.getMergeCommit()
        );
    }

    public static List<CommitInfo> toCommitInfos(List<CommitEntity> entities) {
        return entities == null ? List.of() : entities.stream().map(CommitMapper::toCommitInfo).collect(Collectors.toList());
    }

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

}
