package com.oobeya.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oobeya.github.api.request.GetReposRequest;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.application.port.RepoServicePort;

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
        List<RepoInitrequest> repos = repoService.initUserRepos(login);
        return ResponseEntity.ok(repos);
    }

    @PostMapping("/query")
    public ResponseEntity<List<RepoDto>> getRepos(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetReposRequest request
    ) {
        List<RepoDto> repos = repoService.getRepos(login, request.ownerType(), request.ownerLogin());
        return ResponseEntity.ok(repos);
    }
}
