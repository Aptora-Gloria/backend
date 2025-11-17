package com.oobeya.github.application.port;

import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;


import java.util.List;

public interface RepoServicePort {

    List<RepoInitrequest> initUserRepos(String authToken);
    List<RepoDto> getReposFromDb(String ownerLogin);
    List<RepoDto> getReposForCurrentUser(String authToken, OwnerType ownerType, String ownerLogin);
}
