package com.optimetric.github.application.client;

import com.optimetric.github.api.request.RepoInitrequest;

import java.util.List;

public interface RepoClientPort {
    List<RepoInitrequest> getUserRepos(String accessToken);
}

