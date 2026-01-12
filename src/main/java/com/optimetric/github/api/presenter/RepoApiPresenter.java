package com.optimetric.github.api.presenter;

import java.util.List;

import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.usecase.InitializeUserRepositoriesUseCase;
import com.optimetric.github.application.usecase.QueryRepositoriesUseCase;
import com.optimetric.github.infrastructure.core.mapper.ApplicationToApiMapper;
import com.optimetric.shared.domain.OwnerType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * API Presenter for Repository operations.
 * Handles the presentation logic (mapping) between Application layer and API layer.
 * This allows the Controller to focus solely on HTTP concerns.
 */
@Component
@RequiredArgsConstructor
public class RepoApiPresenter {

    private final InitializeUserRepositoriesUseCase initializeUserRepositoriesUseCase;
    private final QueryRepositoriesUseCase queryRepositoriesUseCase;

    /**
     * Initializes user repositories and returns API DTOs.
     * 
     * @param login User login
     * @return List of repository initialization API DTOs
     */
    public List<RepoInitRequest> initUserRepos(String login) {
        List<RepositoryInitData> repos = initializeUserRepositoriesUseCase.execute(login);
        return ApplicationToApiMapper.toRepoInitRequests(repos);
    }

    /**
     * Queries repositories and returns API DTOs.
     * 
     * @param login User login
     * @param request API request DTO
     * @return List of repository API DTOs
     */
    public List<RepoDto> getRepos(String login, GetReposRequest request) {
        // Extract application-level parameters from API request
        OwnerType ownerType = ApplicationToApiMapper.extractOwnerType(request);
        String ownerLogin = ApplicationToApiMapper.extractOwnerLogin(request);
        
        // Execute use case
        List<RepositoryInfo> repos = queryRepositoriesUseCase.execute(login, ownerType, ownerLogin);
        
        // Map to API DTOs
        return ApplicationToApiMapper.toRepoDtos(repos);
    }
}
