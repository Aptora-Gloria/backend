package com.optimetric.github.application.port;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;

public interface RepoServicePort {

    /**
     * Initializes user repositories from GitHub API.
     * 
     * @param login User login
     * @return List of repository initialization data
     * @throws UserNotFoundException if user not found
     */
    List<RepositoryInitData> initUserRepos(String login) throws UserNotFoundException;
    
    /**
     * Gets repositories from database by owner login.
     * 
     * @param ownerLogin Owner login
     * @return List of repository information
     */
    List<RepositoryInfo> getReposFromDb(String ownerLogin);
    
    /**
     * Gets repositories for user with optional filters.
     * 
     * @param login User login
     * @param ownerType Owner type filter (optional)
     * @param ownerLogin Owner login filter (optional)
     * @return List of repository information
     * @throws UserNotFoundException if user not found
     */
    List<RepositoryInfo> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException;
    
    /**
     * Gets repository by fullName for current user.
     * 
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return Optional RepositoryInfo
     * @throws UserNotFoundException if user not found
     */
    Optional<RepositoryInfo> getByFullNameForCurrentUser(String login, String fullName) throws UserNotFoundException;
    
    /**
     * Gets repository by fullName for current user or throws RepositoryNotFoundException.
     * This method should be used when repository existence is required.
     * 
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return RepositoryInfo
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    RepositoryInfo getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
}
