package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.client.GitHubRepoQueryPort;
import com.optimetric.github.application.port.UserContextPort;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.application.port.RepositoryPersistencePort;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepoServiceAdapter implements RepoServicePort {

    private final RepositoryPersistencePort repositoryPersistencePort;
    private final GitHubRepoQueryPort githubRepoQueryPort;
    private final UserContextPort userContextPort;
    // AuthServicePort is still used for non-init operations that require User entity
    // This is acceptable as ISSUE 5 only targets the init flow
    private final AuthServicePort authServicePort;

    @Override
    public List<RepositoryInitData> initUserRepos(String login) throws UserNotFoundException {
        // Get user context (userId, login, accessToken) through port
        var userContext = userContextPort.getContextByLogin(login);

        List<RepositoryInitData> repos = githubRepoQueryPort.getUserRepos(userContext.accessToken());
        // Save repos using persistence port (includes deduplication logic)
        repositoryPersistencePort.saveAll(repos, userContext.userId());
        return repos;
    }

    @Override
    public List<RepositoryInfo> getReposFromDb(String ownerLogin) {
        return repositoryPersistencePort.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin);
    }

    @Override
    public List<RepositoryInfo> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException {
        // For non-init operations, we still use AuthServicePort as they're not part of the init flow
        // ISSUE 5 only targets the init flow (initUserRepos method)
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByUserWithCriteria(user, ownerType, ownerLogin);
    }

    @Override
    public Optional<RepositoryInfo> getByFullNameForCurrentUser(String login, String fullName) throws UserNotFoundException {
        // For non-init operations, we still use AuthServicePort as they're not part of the init flow
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByFullNameAndUser(fullName, user);
    }

    @Override
    public RepositoryInfo getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        // For non-init operations, we still use AuthServicePort as they're not part of the init flow
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByFullNameAndUser(fullName, user)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
    }

}
