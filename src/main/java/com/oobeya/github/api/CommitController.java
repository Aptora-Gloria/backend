package com.oobeya.github.api;

import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.request.CommitInitRequest;
import com.oobeya.github.application.port.CommitServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github/commits")
@RequiredArgsConstructor
public class CommitController {
    
    private final CommitServicePort commitService;

    @PostMapping("/init")
    public ResponseEntity<List<CommitDto>> initCommits(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommitInitRequest request
    ) {
        List<CommitDto> commits = commitService.fetchAndSaveCommits(
                authHeader,
                request.fullName(),
                request.branchName(),
                request.since()
        );
        return ResponseEntity.ok(commits);
    }

    @GetMapping
    public ResponseEntity<List<CommitDto>> getCommits(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("fullName") String fullName,
            @RequestParam(required = false) String branchName
    ) {
        List<CommitDto> commits;
        if (branchName != null && !branchName.isBlank()) {
            commits = commitService.getCommitsByFullNameAndBranch(authHeader, fullName, branchName);
        } else {
            commits = commitService.getCommitsByFullName(authHeader, fullName);
        }
        return ResponseEntity.ok(commits);
    }
}
