package com.oobeya.github.controller;


import com.oobeya.github.dto.response.RepoDto;
import com.oobeya.github.service.RepoServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoServiceI repoService;

    @PostMapping("/init/user")
    public ResponseEntity<List<RepoDto>> initUserRepos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("login") String login
    ) {
        List<RepoDto> repos = repoService.initUserRepos(authHeader, login);
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
