package com.optimetric.github.application.port;

import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;
import com.optimetric.github.api.request.RepoInitrequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;


import java.util.List;
import java.util.Optional;

public interface RepoServicePort {

    List<RepoInitrequest> initUserRepos(String login);
    List<RepoDto> getReposFromDb(String ownerLogin);
    List<RepoDto> getRepos(String login, OwnerType ownerType, String ownerLogin);
    Optional<RepositoryEntity> getByFullNameForCurrentUser(String login, String fullName);
}
