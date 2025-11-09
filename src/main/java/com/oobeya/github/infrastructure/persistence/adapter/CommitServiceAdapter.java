package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.infrastructure.persistence.entity.CommitEntity;
import com.oobeya.github.infrastructure.core.mapper.CommitMapper;
import com.oobeya.github.infrastructure.persistence.jpa.CommitRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.CommitServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommitServiceAdapter implements CommitServicePort {

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final CommitRepository repository;

    @Override
    public List<CommitDto> fecthAndSaveCommits(String authHeader, String owner, String repo) {
        String accessToken = authServicePort.getAccessTokenFromToken(authHeader);

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
