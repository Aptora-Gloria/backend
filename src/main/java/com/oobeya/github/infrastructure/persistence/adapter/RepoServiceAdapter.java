package com.oobeya.github.infrastructure.persistence.adapter;


import java.util.List;

import org.springframework.stereotype.Service;

import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.auth.infrastructure.persistence.entity.User;
import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.core.mapper.RepoMapper;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.RepoServicePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepoServiceAdapter implements RepoServicePort {

    private final RepositoryRepository repositoryRepo;
    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;

    @Override
    public List<RepoInitrequest> initUserRepos(String authToken) {
        String accessToken = authServicePort.getAccessTokenFromToken(authToken);
        String bearerStripped = authToken != null && authToken.startsWith("Bearer ") ? authToken.substring(7).trim() : authToken;
        String login = authServicePort.getLoginFromToken(bearerStripped);
        User user = authServicePort.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));

        List<RepoInitrequest> repos = apiClient.getUserRepos(accessToken);
        saveRepos(repos, user);
        return repos;
    }

    @Override
    public List<RepoDto> getReposFromDb(String ownerLogin) {
        return repositoryRepo.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin)
                .stream()
                .map(RepoMapper::toDto)
                .toList();
    }

    @Override
    public List<RepoDto> getReposForCurrentUser(String authToken, OwnerType ownerType, String ownerLogin) {
        String bearerStripped = authToken != null && authToken.startsWith("Bearer ") ? authToken.substring(7).trim() : authToken;
        String login = authServicePort.getLoginFromToken(bearerStripped);
        User user = authServicePort.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));

        List<RepositoryEntity> entities;

        if (ownerType == null && ownerLogin == null) {
            entities = repositoryRepo.findByUserOrderByPushedAtDesc(user);
        } else if (ownerType != null && ownerLogin == null) {
            entities = repositoryRepo.findByUserAndOwnerTypeOrderByPushedAtDesc(user, ownerType);
        } else if (ownerType != null && ownerLogin != null) {
            entities = repositoryRepo.findByUserAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(user, ownerType, ownerLogin);
        } else {
            entities = repositoryRepo.findByUserOrderByPushedAtDesc(user);
        }

        return entities.stream()
                .map(RepoMapper::toDto)
                .toList();
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
