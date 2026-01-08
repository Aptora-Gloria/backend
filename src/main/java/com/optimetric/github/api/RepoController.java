package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitrequest;
import com.optimetric.github.api.response.RepoDto;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.ApplicationToApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoServicePort repoService;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoInitrequest>> initUserRepos(
            @AuthenticationPrincipal String login
    ) {
        // Application layer returns RepositoryInitData
        List<RepositoryInitData> repos = repoService.initUserRepos(login);
        // Map to API DTO
        List<RepoInitrequest> apiRepos = ApplicationToApiMapper.toRepoInitRequests(repos);
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
        // Application layer returns RepositoryInfo
        List<RepositoryInfo> repos = repoService.getRepos(login, ownerType, request.ownerLogin());
        // Map to API DTO
        List<RepoDto> apiRepos = ApplicationToApiMapper.toRepoDtos(repos);
        return ResponseEntity.ok(apiRepos);
    }
}
