package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.infrastructure.http.dto.GitHubRepoResponse;

import java.util.List;
import java.util.stream.Collectors;

public class GitHubRepositoryMapper {

    public static RepositoryInitData toRepositoryInitData(GitHubRepoResponse response) {
        if (response == null) return null;
        return new RepositoryInitData(
                response.id(),
                response.nodeId(),
                response.name(),
                response.fullName(),
                response.owner() != null ? new RepositoryInitData.Owner(
                        response.owner().login(),
                        response.owner().nodeId(),
                        response.owner().type()
                ) : null,
                response.description(),
                response.htmlUrl(),
                response.isPrivate(),
                response.fork(),
                response.stargazersCount(),
                response.watchersCount(),
                response.forksCount(),
                response.openIssuesCount(),
                response.size(),
                response.language(),
                response.defaultBranch(),
                response.createdAt(),
                response.updatedAt(),
                response.pushedAt(),
                response.permissions() != null ? new RepositoryInitData.Permissions(
                        response.permissions().admin(),
                        response.permissions().maintain(),
                        response.permissions().push(),
                        response.permissions().triage(),
                        response.permissions().pull()
                ) : null
        );
    }

    public static List<RepositoryInitData> toRepositoryInitDataList(List<GitHubRepoResponse> responses) {
        return responses.stream()
                .map(GitHubRepositoryMapper::toRepositoryInitData)
                .collect(Collectors.toList());
    }
}

