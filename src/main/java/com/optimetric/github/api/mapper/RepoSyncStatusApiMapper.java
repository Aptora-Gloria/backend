package com.optimetric.github.api.mapper;

import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.github.api.response.RepositoryReadinessFlags;
import com.optimetric.github.application.dto.RepositoryReadinessInfo;

/**
 * Maps Application DTO to API DTO for Repository Sync Status endpoint.
 */
public final class RepoSyncStatusApiMapper {

    private RepoSyncStatusApiMapper() {}

    public static RepositoryReadinessDto toRepositoryReadinessDto(RepositoryReadinessInfo info) {
        if (info == null) return null;
        RepositoryReadinessInfo.RepositoryReadinessFlagsInfo f = info.flags();
        RepositoryReadinessFlags flags = f == null
                ? new RepositoryReadinessFlags(false, false, false, false, false, false)
                : new RepositoryReadinessFlags(
                f.branchesReady(),
                f.commitsReady(),
                f.pullRequestsReady(),
                f.prDetailsReady(),
                f.reviewsReady(),
                f.languagesReady());
        return new RepositoryReadinessDto(info.state(), info.missing(), flags);
    }
}
