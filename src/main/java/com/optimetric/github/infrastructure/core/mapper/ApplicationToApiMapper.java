package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.domain.OwnerType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between application DTOs and API DTOs.
 * This mapper is used in the controller layer to map application contracts to API contracts.
 * 
 * Supports bidirectional mapping:
 * - Application → API: Maps application DTOs to API DTOs for responses
 * - API → Application: Maps API request DTOs to application-level parameters
 */
public class ApplicationToApiMapper {

    /**
     * Extracts OwnerType from GetReposRequest for use case execution.
     * This method handles the API → Application mapping for request parameters.
     * 
     * @param request API request DTO
     * @return OwnerType from shared domain (can be null)
     */
    public static OwnerType extractOwnerType(GetReposRequest request) {
        return request != null ? request.ownerType() : null;
    }

    /**
     * Extracts ownerLogin from GetReposRequest for use case execution.
     * 
     * @param request API request DTO
     * @return Owner login string (can be null)
     */
    public static String extractOwnerLogin(GetReposRequest request) {
        return request != null ? request.ownerLogin() : null;
    }

    /**
     * Maps RepositoryInfo (application) to RepoDto (API).
     */
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
                info.ownerType(), // Shared domain OwnerType, no conversion needed
                info.lastSyncedAt()
        );
    }

    /**
     * Maps list of RepositoryInfo to list of RepoDto.
     */
    public static List<RepoDto> toRepoDtos(List<RepositoryInfo> infos) {
        return infos.stream()
                .map(ApplicationToApiMapper::toRepoDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps RepositoryInitData (application) to RepoInitRequest (API).
     */
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

    /**
     * Maps list of RepositoryInitData to list of RepoInitRequest.
     */
    public static List<RepoInitRequest> toRepoInitRequests(List<RepositoryInitData> dataList) {
        return dataList.stream()
                .map(ApplicationToApiMapper::toRepoInitRequest)
                .collect(Collectors.toList());
    }
}

