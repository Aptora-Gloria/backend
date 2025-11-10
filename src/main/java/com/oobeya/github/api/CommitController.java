package com.oobeya.github.api;

import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.request.CommitInitRequest;
import com.oobeya.github.application.port.CommitServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class CommitController {
    
    private final CommitServicePort commitService;

    @PostMapping("/commits/init")
    public ResponseEntity<List<CommitDto>> initCommits(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommitInitRequest request
    ) {
        List<CommitDto> commits = commitService.fetchAndSaveCommits(
                authHeader,
                request.repositoryId(),
                request.branchId()
        );
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/commits/repository/{repositoryId}")
    public ResponseEntity<List<CommitDto>> getCommitsByRepository(
            @PathVariable("repositoryId") Long repositoryId
    ) {
        List<CommitDto> commits = commitService.getCommitsByRepositoryId(repositoryId);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/commits/branch/{branchId}")
    public ResponseEntity<List<CommitDto>> getCommitsByBranch(
            @PathVariable("branchId") Long branchId
    ) {
        List<CommitDto> commits = commitService.getCommitsByBranchId(branchId);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/commits/repository/{repositoryId}/branch/{branchId}")
    public ResponseEntity<List<CommitDto>> getCommitsByRepositoryAndBranch(
            @PathVariable("repositoryId") Long repositoryId,
            @PathVariable("branchId") Long branchId
    ) {
        List<CommitDto> commits = commitService.getCommitsByRepositoryAndBranch(repositoryId, branchId);
        return ResponseEntity.ok(commits);
    }
}
