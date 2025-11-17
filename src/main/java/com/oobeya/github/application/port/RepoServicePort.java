package com.oobeya.github.application.port;

import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;


import java.util.List;
import java.util.Optional;

public interface RepoServicePort {

    List<RepoInitrequest> initUserRepos(String authToken);
    List<RepoDto> getReposFromDb(String ownerLogin);
    List<RepoDto> getReposForCurrentUser(String authToken, OwnerType ownerType, String ownerLogin);
    Optional<RepositoryEntity> getByFullNameForCurrentUser(String authToken, String fullName);
}
