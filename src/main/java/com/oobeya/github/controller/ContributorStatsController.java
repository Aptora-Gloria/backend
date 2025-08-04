package com.oobeya.github.controller;

import com.oobeya.github.dto.ContributorStatsDto;
import com.oobeya.github.dto.ContributorStatsInitRequest;
import com.oobeya.github.service.ContributorStatsServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class ContributorStatsController {

    private final ContributorStatsServiceI statsService;

    @PostMapping("/stats/init")
    public ResponseEntity<List<ContributorStatsDto>> initContributorStats(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ContributorStatsInitRequest request) {
        return ResponseEntity.ok(
                statsService.fetchAndSaveContributorStats(
                        authHeader,
                        request.getOwner(),
                        request.getRepo()
                )
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ContributorStatsDto>> getContributorStats(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String owner,
            @RequestParam String repo) {
        return ResponseEntity.ok(statsService.getContributorStats(authHeader, owner, repo));
    }
}
