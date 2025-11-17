package com.oobeya.github.api;

import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.application.port.RepoServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<RepoDto>> getRepos(
            @AuthenticationPrincipal String login,
            @RequestParam(required = false) OwnerType ownerType,
            @RequestParam(required = false) String ownerLogin
    ) {
        List<RepoDto> repos = repoService.getRepos(login, ownerType, ownerLogin);
        return ResponseEntity.ok(repos);
    }
}
