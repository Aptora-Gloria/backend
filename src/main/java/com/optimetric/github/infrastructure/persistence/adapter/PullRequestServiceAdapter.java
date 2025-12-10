package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.application.client.PullRequestClientPort;
import com.optimetric.github.application.port.PullRequestServicePort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.PullRequestMapper;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PullRequestServiceAdapter implements PullRequestServicePort {

    private final PullRequestRepository pullRequestRepository;
    private final RepoServicePort repoServicePort;
    private final PullRequestClientPort pullRequestClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<PullRequestDto> initPullRequests(String login, String fullName, String state) 
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);

        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        List<PullRequestDto> pullRequests = pullRequestClient.getPullRequests(accessToken, owner, repoName, state);

        savePullRequests(pullRequests, repository);
        return getPullRequestsByRepository(repository, state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestDto> getPullRequestsByFullName(String login, String fullName, String state) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);
        return getPullRequestsByRepository(repository, state);
    }

    private List<PullRequestDto> getPullRequestsByRepository(RepositoryEntity repository, String state) {
        List<PullRequestEntity> entities = pullRequestRepository.findByRepository(repository);
        
        if (state != null && !state.isBlank() && !state.equalsIgnoreCase("all")) {
            entities = entities.stream()
                    .filter(pr -> state.equalsIgnoreCase(pr.getState()))
                    .toList();
        }
        
        return PullRequestMapper.toDtos(entities);
    }

    private void savePullRequests(List<PullRequestDto> pullRequests, RepositoryEntity repository) {
        if (pullRequests == null || pullRequests.isEmpty()) {
            return;
        }

        List<PullRequestEntity> existing = pullRequestRepository.findByRepository(repository);
        Set<Long> existingGithubPrIds = existing.stream()
                .map(PullRequestEntity::getGithubPrId)
                .collect(Collectors.toSet());

        List<PullRequestEntity> toSave = new ArrayList<>();
        for (PullRequestDto dto : pullRequests) {
            if (dto.githubPrId() == null) {
                continue;
            }

            if (existingGithubPrIds.contains(dto.githubPrId())) {
                continue;
            }

            PullRequestEntity entity = PullRequestMapper.toEntity(dto, repository);
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            pullRequestRepository.saveAll(toSave);
        }
    }
}


