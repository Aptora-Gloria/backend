package com.optimetric.github.application.dto;

import com.optimetric.github.domain.enumeration.MissingComponent;
import com.optimetric.github.domain.enumeration.ReadinessState;

import java.util.List;

public record RepositoryReadinessInfo(
        ReadinessState state,
        List<MissingComponent> missing,
        RepositoryReadinessFlagsInfo flags
) {
    public record RepositoryReadinessFlagsInfo(
            boolean branchesReady,
            boolean commitsReady,
            boolean pullRequestsReady,
            boolean prDetailsReady,
            boolean reviewsReady,
            boolean languagesReady
    ) {}
}
