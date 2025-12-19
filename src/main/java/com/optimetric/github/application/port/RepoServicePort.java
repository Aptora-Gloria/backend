package com.optimetric.github.application.port;

import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;
import com.optimetric.github.api.request.RepoInitrequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;

public interface RepoServicePort {

    List<RepoInitrequest> initUserRepos(String login) throws UserNotFoundException;
    List<RepoDto> getReposFromDb(String ownerLogin);
    List<RepoDto> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException;
    Optional<RepositoryEntity> getByFullNameForCurrentUser(String login, String fullName);
    
    /**
     * Gets repository by fullName for current user or throws RepositoryNotFoundException.
     * This method should be used when repository existence is required.
     * 
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return RepositoryEntity
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    RepositoryEntity getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
}
