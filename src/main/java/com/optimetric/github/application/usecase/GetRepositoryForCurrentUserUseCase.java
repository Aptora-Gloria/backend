package com.optimetric.github.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.application.dto.UserInfo;
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
     * Note: This method first tries to find repository by fullName and userId.
     * If found, user has access (because they synced it, including org repos).
     * If not found, tries to find by fullName only and applies domain-level access validation.
     * This allows access to:
     * - User's own repositories (where user is owner)
     * - Organization repositories (where user synced the repo but org is owner)
     * - Public repositories (accessible to everyone)
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
        
        // Get user info (Application DTO, not entity)
        UserInfo userInfo = authServicePort.findByLoginOrThrow(login);
        
        // First, try to find repository by fullName and userId
        // This finds repos that the user synced (including org repos where user synced but org is owner)
        Optional<RepositoryInfo> repository = repositoryPersistencePort.findByFullNameAndUser(
                fullName, userInfo.id());
        
        // If found by user, user has access (they synced it, so they can access it)
        if (repository.isPresent()) {
            return repository;
        }
        
        // If not found by user, try to find by fullName only (for public repos or other cases)
        repository = repositoryPersistencePort.findByFullName(fullName);
        
        // If repository not found, return empty
        if (repository.isEmpty()) {
            return Optional.empty();
        }
        
        // Apply domain-level access validation (business rule)
        // This checks if user can access based on ownership, visibility, etc.
        // (e.g., public repos are accessible, private repos only to owner)
        if (!repositoryDomainService.canUserAccessRepository(login, repository.get())) {
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
                .orElseThrow(() -> new RepositoryNotFoundException(
                        "Repository not found with fullName: " + fullName + 
                        " for user: " + login + 
                        ". Please ensure the repository is initialized first."));
    }
}
