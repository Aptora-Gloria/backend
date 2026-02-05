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
import com.optimetric.github.api.mapper.RepoApiMapper;
import com.optimetric.github.application.port.in.InitializeUserRepositoriesPort;
import com.optimetric.github.application.port.in.QueryRepositoriesPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Repo işlemleri REST controller; port/use case çağırır, ApiMapper ile DTO döner.
@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final InitializeUserRepositoriesPort initializeUserRepositoriesPort;
    private final QueryRepositoriesPort queryRepositoriesPort;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoInitRequest>> initUserRepos(
            @AuthenticationPrincipal String login
    ) {
        var list = initializeUserRepositoriesPort.execute(login);
        return ResponseEntity.ok(RepoApiMapper.toRepoInitRequests(list));
    }

    @PostMapping("/query")
    public ResponseEntity<List<RepoDto>> getRepos(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetReposRequest request
    ) {
        var ownerType = RepoApiMapper.extractOwnerType(request);
        var ownerLogin = RepoApiMapper.extractOwnerLogin(request);
        var list = queryRepositoriesPort.execute(login, ownerType, ownerLogin);
        return ResponseEntity.ok(RepoApiMapper.toRepoDtos(list));
    }
}
