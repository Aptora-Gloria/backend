package com.optimetric.github.application.port.out;

import java.util.List;

import com.optimetric.github.application.dto.CommitInfo;

public interface CommitClientPort {
    List<CommitInfo> getCommits(String accessToken, String owner, String repo, String branch, String since);
}
