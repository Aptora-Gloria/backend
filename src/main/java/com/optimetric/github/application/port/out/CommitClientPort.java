package com.optimetric.github.application.port.out;

import com.optimetric.github.api.response.CommitDto;

import java.util.List;

/**
 * Outbound port for fetching commits from GitHub API.
 */
public interface CommitClientPort {
    List<CommitDto> getCommits(String accessToken, String owner, String repo, String branch, String since);
}
