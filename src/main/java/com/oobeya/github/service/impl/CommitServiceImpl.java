package com.oobeya.github.service.impl;

import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.response.CommitDto;
import com.oobeya.github.entity.CommitEntity;
import com.oobeya.github.mapper.CommitMapper;
import com.oobeya.github.repository.CommitRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.CommitServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommitServiceImpl implements CommitServiceI {

    private final ApiClientI apiClient;
    private final AuthService authService;
    private final CommitRepository repository;

    @Override
    public List<CommitDto> fecthAndSaveCommits(String authHeader, String owner, String repo) {
        String accessToken = authService.getAccessTokenFromToken(authHeader);

        List<CommitDto> commits = apiClient.getCommits(accessToken, owner, repo);
        List<CommitEntity> entities = CommitMapper.toEntities(commits, repo);//TODO commit maper taşıncak
        saveCommits(entities);
        return commits;
    }

    @Override
    public List<CommitDto> getCommitsFromDb(String author, String repo) {
        return repository.findByRepoName(repo).stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    public void saveCommits(  List<CommitEntity> entities) {
        repository.saveAll(entities);
    }
}
