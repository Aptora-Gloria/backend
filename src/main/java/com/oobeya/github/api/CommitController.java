package com.oobeya.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oobeya.github.api.request.CommitInitRequest;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.application.port.CommitServicePort;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/commits")
@RequiredArgsConstructor
public class CommitController {
    
    private final CommitServicePort commitService;

    @PostMapping("/init")
    public ResponseEntity<List<CommitDto>> initCommits(
            @AuthenticationPrincipal String login,
            @RequestBody CommitInitRequest request
    ) {
        List<CommitDto> commits = commitService.initCommits(
                login,
                request.fullName(),
                request.branchName(),
                request.since()
        );
        return ResponseEntity.ok(commits);
    }

    @GetMapping
    public ResponseEntity<List<CommitDto>> getCommits(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName,
            @RequestParam(required = false) String branchName
    ) {
        List<CommitDto> commits;
        if (branchName != null && !branchName.isBlank()) {
            commits = commitService.getCommitsByFullNameAndBranch(login, fullName, branchName);
        } else {
            commits = commitService.getCommitsByFullName(login, fullName);
        }
        return ResponseEntity.ok(commits);
    }
}
