package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.client.RepoClientPort;
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
    private final RepoClientPort repoClient;
    private final AuthServicePort authServicePort;

    @Override
    public List<RepositoryInitData> initUserRepos(String login) throws UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        User user = authServicePort.findByLoginOrThrow(login);

        List<RepositoryInitData> repos = repoClient.getUserRepos(accessToken);
        // Save repos using persistence port (includes deduplication logic)
        repositoryPersistencePort.saveAll(repos, user);
        return repos;
    }

    @Override
    public List<RepositoryInfo> getReposFromDb(String ownerLogin) {
        return repositoryPersistencePort.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin);
    }

    @Override
    public List<RepositoryInfo> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByUserWithCriteria(user, ownerType, ownerLogin);
    }

    @Override
    public Optional<RepositoryInfo> getByFullNameForCurrentUser(String login, String fullName) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByFullNameAndUser(fullName, user);
    }

    @Override
    public RepositoryInfo getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryPersistencePort.findByFullNameAndUser(fullName, user)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
    }

}
