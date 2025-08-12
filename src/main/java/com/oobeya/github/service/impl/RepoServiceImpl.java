package com.oobeya.github.service.impl;

import com.oobeya.auth.entity.OwnerType;
import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.response.RepoDto;

import com.oobeya.github.entity.RepositoryEntity;
import com.oobeya.github.mapper.RepoMapper;
import com.oobeya.github.repository.RepositoryRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.RepoServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoServiceImpl implements RepoServiceI {

    private final RepositoryRepository repositoryRepo;
    private final ApiClientI apiClient;
    private final AuthService authService;


    @Override
    public List<RepoDto> initUserRepos(String authToken, String ownerLogin) {
        String accessToken = authService.getAccessTokenFromToken(authToken);
        List<RepoDto> repos = apiClient.getUserRepos(accessToken);

        saveRepos(repos,ownerLogin);
        return repos;
    }

    @Override
    public List<RepoDto> getReposFromDb(String ownerLogin) {
        return repositoryRepo.findByLogin(ownerLogin)
                .stream()
                .map(RepoMapper::toDto)
                .toList();
    }

    private void saveRepos(List<RepoDto> repos, String ownerLogin) {

        List<String> incomingUrls = repos.stream()
                .map(RepoDto::htmlUrl)
                .toList();

        // 2. Veritabanında bu url'lere sahip olanları çek
        List<RepositoryEntity> existingEntities = repositoryRepo.findAllByHtmlUrlIn(incomingUrls);
        List<String> existingUrls = existingEntities.stream()
                .map(RepositoryEntity::getHtmlUrl)
                .toList();

        // 3. Daha önce eklenmemiş olanları filtrele
        List<RepoDto> newRepos = repos.stream()
                .filter(repo -> !existingUrls.contains(repo.htmlUrl()))
                .toList();


        List<RepositoryEntity> entitiesToSave = RepoMapper.toEntities(newRepos, ownerLogin);


        repositoryRepo.saveAll(entitiesToSave);
    }

}
