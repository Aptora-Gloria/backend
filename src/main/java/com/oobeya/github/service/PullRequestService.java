package com.oobeya.github.service;

import com.oobeya.github.domain.pullrequest.PullRequest;
import com.oobeya.github.dto.request.PullRequestListRequest;
import com.oobeya.github.dto.request.PullRequestSyncRequest;

import java.util.List;
import java.util.Optional;

public interface PullRequestService {

    List<PullRequest> listPullRequests(PullRequestListRequest request);

    Optional<PullRequest> getPullRequestDetail(Long repositoryId, Integer pullRequestNumber);

    void syncPullRequests(PullRequestSyncRequest request);
}

