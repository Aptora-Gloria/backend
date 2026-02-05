package com.optimetric.github.infrastructure.core.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.application.dto.PullRequestCommitFromApi;
import com.optimetric.github.application.dto.PullRequestDetailFromApi;
import com.optimetric.github.application.dto.PullRequestFromApi;
import com.optimetric.github.application.dto.PullRequestReviewFromApi;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestCommitResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestDetailResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubPullRequestReviewResponse;

public final class PullRequestFromApiMapper {

    private PullRequestFromApiMapper() {}

    public static PullRequestFromApi toPullRequestFromApi(GitHubPullRequestResponse r) {
        if (r == null) return null;
        PullRequestFromApi.AuthorInfo user = r.user() != null
                ? new PullRequestFromApi.AuthorInfo(r.user().id(), r.user().login())
                : null;
        return new PullRequestFromApi(
                r.id(), r.number(), r.state(), r.draft(), r.title(), r.body(),
                r.htmlUrl(), r.mergeCommitSha(), r.createdAt(), r.updatedAt(), r.closedAt(), r.mergedAt(),
                user
        );
    }

    public static List<PullRequestFromApi> toPullRequestFromApiList(List<GitHubPullRequestResponse> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestFromApiMapper::toPullRequestFromApi).collect(Collectors.toList());
    }

    public static PullRequestDetailFromApi toPullRequestDetailFromApi(GitHubPullRequestDetailResponse r) {
        if (r == null) return null;
        PullRequestFromApi.AuthorInfo user = r.user() != null
                ? new PullRequestFromApi.AuthorInfo(r.user().id(), r.user().login())
                : null;
        return new PullRequestDetailFromApi(
                r.id(), r.number(), r.state(), r.draft(), r.title(), r.body(),
                r.htmlUrl(), r.mergeCommitSha(), r.createdAt(), r.updatedAt(), r.closedAt(), r.mergedAt(),
                user,
                r.additions(), r.deletions(), r.changedFiles()
        );
    }

    public static PullRequestReviewFromApi toPullRequestReviewFromApi(GitHubPullRequestReviewResponse r) {
        if (r == null) return null;
        PullRequestReviewFromApi.AuthorInfo user = r.user() != null
                ? new PullRequestReviewFromApi.AuthorInfo(r.user().id(), r.user().login())
                : null;
        return new PullRequestReviewFromApi(
                r.id(), user, r.state(), r.body(), r.submittedAt(), r.htmlUrl()
        );
    }

    public static List<PullRequestReviewFromApi> toPullRequestReviewFromApiList(List<GitHubPullRequestReviewResponse> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestFromApiMapper::toPullRequestReviewFromApi).collect(Collectors.toList());
    }

    public static PullRequestCommitFromApi toPullRequestCommitFromApi(GitHubPullRequestCommitResponse r) {
        if (r == null) return null;
        String message = r.commit() != null ? r.commit().message() : null;
        String authorName = r.commit() != null && r.commit().author() != null ? r.commit().author().name() : null;
        java.time.OffsetDateTime committedAt = r.commit() != null && r.commit().author() != null ? r.commit().author().date() : null;
        String authorLogin = r.author() != null ? r.author().login() : null;
        return new PullRequestCommitFromApi(
                r.sha(), message, r.htmlUrl(), authorLogin, authorName, committedAt
        );
    }

    public static List<PullRequestCommitFromApi> toPullRequestCommitFromApiList(List<GitHubPullRequestCommitResponse> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestFromApiMapper::toPullRequestCommitFromApi).collect(Collectors.toList());
    }
}
