package com.oobeya.github.controller;

import com.oobeya.github.dto.RepoDto;
import com.oobeya.github.service.RepoServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github") //VERSİYONlama v1 v2 mi araştır
@RequiredArgsConstructor
public class RepoController {

    private final RepoServiceI repoService;

    @PostMapping("/init")
    public ResponseEntity<List<RepoDto>> initUserRepos(@RequestHeader("Authorization") String authHeader) {
        List<RepoDto> repos = repoService.SaveAndFecthRepos(authHeader);
        return ResponseEntity.ok(repos);
    }

    @GetMapping
    public ResponseEntity<List<RepoDto>> getUserRepos(@RequestHeader("Authorization") String authHeader) {
        List<RepoDto> repos = repoService.getUserReposFromDb(authHeader);
        return ResponseEntity.ok(repos);
    }

}
