package com.optimetric.github.application.client;

import com.optimetric.github.application.dto.RepositoryInitData;

import java.util.List;

public interface RepoClientPort {
    List<RepositoryInitData> getUserRepos(String accessToken);
}

