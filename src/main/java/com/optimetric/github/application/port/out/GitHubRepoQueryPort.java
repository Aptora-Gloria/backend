package com.optimetric.github.application.port.out;

import java.util.List;

import com.optimetric.github.application.dto.RepositoryInitData;

public interface GitHubRepoQueryPort {
    List<RepositoryInitData> getUserRepos(String accessToken);
}

