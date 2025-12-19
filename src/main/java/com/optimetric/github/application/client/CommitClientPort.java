package com.optimetric.github.application.client;

import com.optimetric.github.api.response.CommitDto;

import java.util.List;

public interface CommitClientPort {
    List<CommitDto> getCommits(String accessToken, String owner, String repo, String branch, String since);
}

