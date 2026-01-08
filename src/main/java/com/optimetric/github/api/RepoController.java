package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.usecase.GetRepositoryForCurrentUserUseCase;
import com.optimetric.github.application.usecase.InitializeUserRepositoriesUseCase;
import com.optimetric.github.application.usecase.QueryRepositoriesUseCase;
import com.optimetric.github.infrastructure.core.mapper.ApplicationToApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final InitializeUserRepositoriesUseCase initializeUserRepositoriesUseCase;
    private final QueryRepositoriesUseCase queryRepositoriesUseCase;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoInitRequest>> initUserRepos(
            @AuthenticationPrincipal String login
    ) {
        // Use case orchestrates the flow and returns RepositoryInitData
        List<RepositoryInitData> repos = initializeUserRepositoriesUseCase.execute(login);
        // Map to API DTO
        List<RepoInitRequest> apiRepos = ApplicationToApiMapper.toRepoInitRequests(repos);
        return ResponseEntity.ok(apiRepos);
    }

    @PostMapping("/query")
    public ResponseEntity<List<RepoDto>> getRepos(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetReposRequest request
    ) {
        // Convert API OwnerType to shared domain OwnerType
        com.optimetric.shared.domain.OwnerType ownerType = request.ownerType() != null 
            ? com.optimetric.shared.domain.OwnerType.valueOf(request.ownerType().name())
            : null;
        // Use case orchestrates the flow and returns RepositoryInfo
        List<RepositoryInfo> repos = queryRepositoriesUseCase.execute(login, ownerType, request.ownerLogin());
        // Map to API DTO
        List<RepoDto> apiRepos = ApplicationToApiMapper.toRepoDtos(repos);
        return ResponseEntity.ok(apiRepos);
    }
}
