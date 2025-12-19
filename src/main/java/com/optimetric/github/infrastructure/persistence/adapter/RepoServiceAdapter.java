package com.optimetric.github.infrastructure.persistence.adapter;


import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.request.RepoInitrequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.core.mapper.RepoMapper;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.github.application.client.RepoClientPort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepoServiceAdapter implements RepoServicePort {

    private final RepositoryRepository repositoryRepo;
    private final RepoClientPort repoClient;
    private final AuthServicePort authServicePort;

    @Override
    public List<RepoInitrequest> initUserRepos(String login) throws UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        User user = authServicePort.findByLoginOrThrow(login);

        List<RepoInitrequest> repos = repoClient.getUserRepos(accessToken);
        saveRepos(repos, user);
        return repos;
    }

    @Override
    public List<RepoDto> getReposFromDb(String ownerLogin) {
        List<RepositoryEntity> entities = repositoryRepo.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin);
        return RepoMapper.toDtos(entities);
    }

    @Override
    public List<RepoDto> getRepos(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> entities = findReposByCriteria(user, ownerType, ownerLogin);
        return RepoMapper.toDtos(entities);
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
    public java.util.Optional<RepositoryEntity> getByFullNameForCurrentUser(String login, String fullName) {
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryRepo.findByFullNameAndUser(fullName, user);
    }

    @Override
    public RepositoryEntity getByFullNameForCurrentUserOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        return repositoryRepo.findByFullNameAndUser(fullName, user)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + fullName));
    }

    private void saveRepos(List<RepoInitrequest> repos, User user) {

        List<String> incomingUrls = repos.stream()
                .map(RepoInitrequest::html_url)
                .toList();

        // 2. Veritabanında bu url'lere sahip olanları çek
        List<RepositoryEntity> existingEntities = repositoryRepo.findAllByHtmlUrlIn(incomingUrls);
        List<String> existingUrls = existingEntities.stream()
                .map(RepositoryEntity::getHtmlUrl)
                .toList();

        // 3. Daha önce eklenmemiş olanları filtrele
        List<RepoInitrequest> newRepos = repos.stream()
                .filter(repo -> !existingUrls.contains(repo.html_url()))
                .toList();


        List<RepositoryEntity> entitiesToSave = RepoMapper.toEntities(newRepos);
        entitiesToSave.forEach(e -> e.setUser(user));
        repositoryRepo.saveAll(entitiesToSave);
    }

}
