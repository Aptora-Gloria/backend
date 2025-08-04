package com.oobeya.github.service.impl;

import com.oobeya.auth.service.AuthService;
import com.oobeya.github.entity.RepositoryEntity;
import com.oobeya.github.mapper.RepoMapper;
import com.oobeya.github.repository.RepositoryRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.RepoServiceI;
import com.oobeya.github.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoServiceImpl implements RepoServiceI {
    private final RepositoryRepository repositoryRepo;


    private final ApiClientI apiClient;
    private  final AuthService authService;

    @Override
    public List<RepoDto> SaveAndFecthRepos(String authToken) {
        String accessToken = authService.getAccessTokenFromToken(authToken);

        List<RepoDto> repos = apiClient.getUserRepos(accessToken);
        saveRepos(repos);
        return repos;
    }

    @Override
    public List<RepoDto> getUserReposFromDb(String authToken) {
        return repositoryRepo.findAll().stream()
                .map(RepoMapper::toDto)
                .toList();
    }


    public void saveRepos(List<RepoDto> repos) {
        List<RepositoryEntity> entities = RepoMapper.toEntities(repos);
        repositoryRepo.saveAll(entities);
    }
}
