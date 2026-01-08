package com.optimetric.github.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Use case for getting a repository by fullName for the current user.
 * Orchestrates the flow of retrieving a specific repository from the database.
 */
@Service
@RequiredArgsConstructor
public class GetRepositoryForCurrentUserUseCase {

    private final AuthServicePort authServicePort;
    private final RepositoryPersistencePort repositoryPersistencePort;

    /**
     * Gets repository by fullName for current user.
     *
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return Optional RepositoryInfo
     * @throws UserNotFoundException if user not found
     */
    public Optional<RepositoryInfo> execute(String login, String fullName) throws UserNotFoundException {
        // Get user entity (required for RepositoryPersistencePort.findByFullNameAndUser)
        User user = authServicePort.findByLoginOrThrow(login);

        // Query repository by fullName and user
        return repositoryPersistencePort.findByFullNameAndUser(fullName, user);
    }

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
    public RepositoryInfo executeOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        return execute(login, fullName)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
    }
}
