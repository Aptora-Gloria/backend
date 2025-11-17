package com.oobeya.github.api;


import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.application.port.RepoServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoServicePort repoService;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoInitrequest>> initUserRepos(
            @RequestHeader("Authorization") String authHeader

    ) {
        List<RepoInitrequest> repos = repoService.initUserRepos(authHeader);
        return ResponseEntity.ok(repos);
    }

    @GetMapping
    public ResponseEntity<List<RepoDto>> getRepos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) OwnerType ownerType,
            @RequestParam(required = false) String ownerLogin
    ) {
        List<RepoDto> repos = repoService.getReposForCurrentUser(authHeader, ownerType, ownerLogin);
        return ResponseEntity.ok(repos);
    }
}
