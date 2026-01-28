package com.optimetric.github.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.out.GitHubRepoQueryPort;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.application.port.out.UserContextPort;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Use case for initializing user repositories from GitHub API.
 * Orchestrates the flow of fetching repositories from GitHub and saving them to the database.
 */
@Service
@RequiredArgsConstructor
public class InitializeUserRepositoriesUseCase {

    private final UserContextPort userContextPort;
    private final GitHubRepoQueryPort githubRepoQueryPort;
    private final RepositoryPersistencePort repositoryPersistencePort;

    /**
     * Initializes user repositories by fetching from GitHub API and saving to database.
     *
     * @param login User login
     * @return List of repository initialization data
     * @throws UserNotFoundException if user not found
     */

    //TODO aplication katmanı altıngeni oluşturan kırmızı çizgilerden bahsediyorsak eğer yani orkestra sınıfı ise
    //TODO 1- doğrudan infra ya erişemez
    //TODO 2. buranın bağımlılığı domaine bağımlı olmak zorunda
    //TODO 3. doamin katmanı infara gitmeli
    public List<RepositoryInitData> execute(String login) throws UserNotFoundException {
        // Get user context (userId, login, accessToken) through port
        var userContextOpt = userContextPort.getContextByLogin(login);
        
        // Handle user not found case (exception thrown by adapter, handled here)
        if (userContextOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with login: " + login);
        }
        
        var userContext = userContextOpt.get();

        // Fetch repositories from GitHub API
        List<RepositoryInitData> repos = githubRepoQueryPort.getUserRepos(userContext.accessToken());

        // Save repos using persistence port (includes deduplication logic)
        repositoryPersistencePort.saveAll(repos, userContext.userId());

        return repos;
    }
}
