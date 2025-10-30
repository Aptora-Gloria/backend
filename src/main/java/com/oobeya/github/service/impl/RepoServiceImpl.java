package com.oobeya.github.service.impl;


import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.request.RepoInitrequest;
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
    public List<RepoInitrequest> initUserRepos(String authToken) {
        String accessToken = authService.getAccessTokenFromToken(authToken);
        List<RepoInitrequest> repos = apiClient.getUserRepos(accessToken);

        saveRepos(repos);
        return repos;
    }

    @Override
    public List<RepoDto> getReposFromDb(String ownerLogin) {
        return repositoryRepo.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin)
                .stream()
                .map(RepoMapper::toDto)
                .toList();
    }

    private void saveRepos(List<RepoInitrequest> repos) {

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


        repositoryRepo.saveAll(entitiesToSave);
    }

}
