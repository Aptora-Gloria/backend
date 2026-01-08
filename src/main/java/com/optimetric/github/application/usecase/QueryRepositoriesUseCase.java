package com.optimetric.github.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Use case for querying repositories with optional filters.
 * Orchestrates the flow of retrieving repositories from the database based on user and filters.
 */
@Service
@RequiredArgsConstructor
public class QueryRepositoriesUseCase {

    private final AuthServicePort authServicePort;
    private final RepositoryPersistencePort repositoryPersistencePort;

    /**
     * Gets repositories for user with optional filters.
     *
     * @param login User login
     * @param ownerType Owner type filter (optional)
     * @param ownerLogin Owner login filter (optional)
     * @return List of repository information
     * @throws UserNotFoundException if user not found
     */
    public List<RepositoryInfo> execute(String login, OwnerType ownerType, String ownerLogin) 
            throws UserNotFoundException {
        // Get user entity (required for RepositoryPersistencePort.findByUserWithCriteria)
        User user = authServicePort.findByLoginOrThrow(login);

        // Query repositories with criteria
        return repositoryPersistencePort.findByUserWithCriteria(user, ownerType, ownerLogin);
    }
}
