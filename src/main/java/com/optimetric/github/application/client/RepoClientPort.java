package com.optimetric.github.application.client;

import java.util.List;

import com.optimetric.github.application.dto.RepositoryInitData;

public interface RepoClientPort {
    List<RepositoryInitData> getUserRepos(String accessToken);
}

