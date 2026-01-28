package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.presenter.RepoApiPresenter;
import com.optimetric.github.api.request.GetReposRequest;
import com.optimetric.github.api.request.RepoInitRequest;
import com.optimetric.github.api.response.RepoDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Repository operations.
 * 
 * Single Responsibility: HTTP boundary only.
 * - Receives HTTP requests
 * - Validates input (@Valid)
 * - Delegates to presenter for business logic and mapping
 * - Returns HTTP responses
 */
@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoApiPresenter presenter;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoInitRequest>> initUserRepos(
            @AuthenticationPrincipal String login
    ) {
        List<RepoInitRequest> repos = presenter.initUserRepos(login);
        return ResponseEntity.ok(repos);
    }

    @PostMapping("/query")
    public ResponseEntity<List<RepoDto>> getRepos(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetReposRequest request
    ) {
        List<RepoDto> repos = presenter.getRepos(login, request);
        return ResponseEntity.ok(repos);
    }
}
