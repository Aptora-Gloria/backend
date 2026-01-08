package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.client.RepoClientPort;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.RepositoryMapper;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepoServiceAdapter implements RepoServicePort {

    private final RepositoryRepository repositoryRepo;
    private final RepoClientPort repoClient;
    private final AuthServicePort authServicePort;

    @Override
    public List<RepositoryInitData> initUserRepos(String login) throws UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        User user = authServicePort.findByLoginOrThrow(login);

        List<RepositoryInitData> repos = repoClient.getUserRepos(accessToken);
        saveRepos(repos, user);
        return repos;
    }

    @Override
    public List<RepositoryInfo> getReposFromDb(String ownerLogin) {
        List<RepositoryEntity> entities = repositoryRepo.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin);
        return RepositoryMapper.toRepositoryInfos(entities);
    }

    @Override
    public List<RepositoryInfo> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> entities = findReposByCriteria(user, ownerType, ownerLogin);
        return RepositoryMapper.toRepositoryInfos(entities);
    }

    private List<RepositoryEntity> findReposByCriteria(User user, OwnerType ownerType, String ownerLogin) {
        if (ownerType != null && ownerLogin != null) {
            return repositoryRepo.findByUserAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(user, ownerType, ownerLogin);
        } else if (ownerType != null) {
            return repositoryRepo.findByUserAndOwnerTypeOrderByPushedAtDesc(user, ownerType);
        } else {
            return repositoryRepo.findByUserOrderByPushedAtDesc(user);
        }
    }

    @Override
    public Optional<RepositoryInfo> getByFullNameForCurrentUser(String login, String fullName) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        Optional<RepositoryEntity> entity = repositoryRepo.findByFullNameAndUser(fullName, user);
        return entity.map(RepositoryMapper::toRepositoryInfo);
    }

    @Override
    public RepositoryInfo getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        RepositoryEntity entity = repositoryRepo.findByFullNameAndUser(fullName, user)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
        return RepositoryMapper.toRepositoryInfo(entity);
    }

    private void saveRepos(List<RepositoryInitData> repos, User user) {
        List<String> incomingUrls = repos.stream()
                .map(RepositoryInitData::htmlUrl)
                .toList();

        // Veritabanında bu url'lere sahip olanları çek
        List<RepositoryEntity> existingEntities = repositoryRepo.findAllByHtmlUrlIn(incomingUrls);
        List<String> existingUrls = existingEntities.stream()
                .map(RepositoryEntity::getHtmlUrl)
                .toList();

        // Daha önce eklenmemiş olanları filtrele
        List<RepositoryInitData> newRepos = repos.stream()
                .filter(repo -> !existingUrls.contains(repo.htmlUrl()))
                .toList();

        List<RepositoryEntity> entitiesToSave = RepositoryMapper.toEntities(newRepos);
        entitiesToSave.forEach(e -> e.setUser(user));
        repositoryRepo.saveAll(entitiesToSave);
    }

}
