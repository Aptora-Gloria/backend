package com.optimetric.github.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.domain.OwnerType;

public final class RepoApiMapper {

    private RepoApiMapper() {}

    public static OwnerType extractOwnerType(GetReposRequest request) {
        return request != null ? request.ownerType() : null;
    }

    public static String extractOwnerLogin(GetReposRequest request) {
        return request != null ? request.ownerLogin() : null;
    }

    public static RepoDto toRepoDto(RepositoryInfo info) {
        if (info == null) return null;
        return new RepoDto(
                info.id(),
                info.githubRepoId(),
                info.name(),
                info.fullName(),
                info.description(),
                info.htmlUrl(),
                info.isPrivate(),
                info.isFork(),
                info.stargazersCount(),
                info.watchersCount(),
                info.forksCount(),
                info.openIssuesCount(),
                info.size(),
                info.language(),
                info.defaultBranch(),
                info.createdAt(),
                info.updatedAt(),
                info.pushedAt(),
                info.ownerLogin(),
                info.ownerType(),
                info.lastSyncedAt()
        );
    }

    public static List<RepoDto> toRepoDtos(List<RepositoryInfo> infos) {
        return infos.stream().map(RepoApiMapper::toRepoDto).collect(Collectors.toList());
    }

    public static RepoInitRequest toRepoInitRequest(RepositoryInitData data) {
        if (data == null) return null;
        return new RepoInitRequest(
                data.id(),
                data.nodeId(),
                data.name(),
                data.fullName(),
                data.owner() != null ? new RepoInitRequest.Owner(
                        data.owner().login(),
                        data.owner().nodeId(),
                        data.owner().type()
                ) : null,
                data.description(),
                data.htmlUrl(),
                data.isPrivate(),
                data.fork(),
                data.stargazersCount(),
                data.watchersCount(),
                data.forksCount(),
                data.openIssuesCount(),
                data.size(),
                data.language(),
                data.defaultBranch(),
                data.createdAt(),
                data.updatedAt(),
                data.pushedAt(),
                data.permissions() != null ? new RepoInitRequest.Permissions(
                        data.permissions().admin(),
                        data.permissions().maintain(),
                        data.permissions().push(),
                        data.permissions().triage(),
                        data.permissions().pull()
                ) : null
        );
    }

    public static List<RepoInitRequest> toRepoInitRequests(List<RepositoryInitData> dataList) {
        return dataList.stream().map(RepoApiMapper::toRepoInitRequest).collect(Collectors.toList());
    }
}
