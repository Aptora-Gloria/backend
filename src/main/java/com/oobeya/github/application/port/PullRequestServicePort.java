package com.oobeya.github.application.port;

import com.oobeya.github.dto.request.PullRequestListRequest;
import com.oobeya.github.dto.request.PullRequestSyncRequest;
import com.oobeya.github.dto.response.PullRequestDto;

import java.util.List;
import java.util.Optional;

public interface PullRequestServicePort {

    List<PullRequestDto> listPullRequests(PullRequestListRequest request);

    Optional<PullRequestDto> getPullRequestDetail(Long repositoryId, Integer pullRequestNumber);

    void syncPullRequests(PullRequestSyncRequest request);
}

