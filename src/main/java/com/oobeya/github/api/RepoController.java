package com.oobeya.github.api;


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
    public ResponseEntity<List<RepoDto>> getReposFromDb(
            @RequestParam("ownerLogin") String ownerLogin
    ) {
        List<RepoDto> repos = repoService.getReposFromDb(ownerLogin);
        return ResponseEntity.ok(repos);
    }
}
