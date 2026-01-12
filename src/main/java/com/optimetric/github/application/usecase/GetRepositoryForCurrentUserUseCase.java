package com.optimetric.github.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.domain.service.RepositoryDomainService;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Use case for getting a repository by fullName for the current user.
 * Orchestrates the flow of retrieving a specific repository from the database.
 * 
 * Delegates domain validation to RepositoryDomainService.
 */
@Service
@RequiredArgsConstructor
public class GetRepositoryForCurrentUserUseCase {

    private final AuthServicePort authServicePort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final RepositoryDomainService repositoryDomainService;

    /**
     * Gets repository by fullName for current user.
     *
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return Optional RepositoryInfo
     * @throws UserNotFoundException if user not found
     * @throws IllegalArgumentException if fullName format is invalid
     */
    public Optional<RepositoryInfo> execute(String login, String fullName) throws UserNotFoundException {
        // Validate fullName format using domain service (business rule)
        repositoryDomainService.validateAndParseFullName(fullName);
        
        // Get user entity (required for RepositoryPersistencePort.findByFullNameAndUser)
        User user = authServicePort.findByLoginOrThrow(login);

        // Query repository by fullName and user
        Optional<RepositoryInfo> repository = repositoryPersistencePort.findByFullNameAndUser(fullName, user);
        
        // Apply domain-level access validation (business rule)
        if (repository.isPresent() && !repositoryDomainService.canUserAccessRepository(login, repository.get())) {
            return Optional.empty();
        }
        
        return repository;
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
     * @throws IllegalArgumentException if fullName format is invalid
     */
    public RepositoryInfo executeOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        return execute(login, fullName)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
    }
}
