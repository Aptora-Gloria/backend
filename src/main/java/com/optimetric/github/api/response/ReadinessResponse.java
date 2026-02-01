package com.optimetric.github.api.response;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hazırlık API yanıtı (reposync endpoint'leri için uyumlu format).
 */
public record ReadinessResponse(
    @JsonProperty("state")
    String state,
    @JsonProperty("missing")
    List<String> missing,
    @JsonProperty("flags")
    ReadinessFlagsResponse flags
) {
    public static ReadinessResponse from(RepositoryReadinessDto dto) {
        List<String> missingStrings = dto.missing().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return new ReadinessResponse(
                dto.state().name(),
                missingStrings,
                ReadinessFlagsResponse.from(dto.flags())
        );
    }

    public record ReadinessFlagsResponse(
        @JsonProperty("branchesReady") boolean branchesReady,
        @JsonProperty("commitsReady") boolean commitsReady,
        @JsonProperty("pullRequestsReady") boolean pullRequestsReady,
        @JsonProperty("prDetailsReady") boolean prDetailsReady,
        @JsonProperty("reviewsReady") boolean reviewsReady,
        @JsonProperty("languagesReady") boolean languagesReady
    ) {
        public static ReadinessFlagsResponse from(RepositoryReadinessFlags f) {
            return new ReadinessFlagsResponse(
                    f.branchesReady(),
                    f.commitsReady(),
                    f.pullRequestsReady(),
                    f.prDetailsReady(),
                    f.reviewsReady(),
                    f.languagesReady()
            );
        }
    }
}
