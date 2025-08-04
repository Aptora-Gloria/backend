package com.oobeya.githubOrg.controller;

import com.oobeya.githubOrg.dto.OrgCommitDto;
import com.oobeya.githubOrg.service.GitHubOrgCommitService;
import com.oobeya.github.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/github/orgs/repos")
@RequiredArgsConstructor
public class GitHubOrgCommitController {

    private final GitHubOrgCommitService gitHubOrgCommitService;

    @GetMapping("/{org}/{repoName}/branches/{branch}/commits")
    public ResponseEntity<List<OrgCommitDto>> getRepoCommitsByBranch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("org") String organization,
            @PathVariable("repoName") String repoName,
            @PathVariable("branch") String branch) {

        String jwtToken = TokenUtil.extractToken(authHeader);
        List<OrgCommitDto> commits = gitHubOrgCommitService.getCommitsOfOrgRepoBranch(jwtToken, organization, repoName, branch);
        return ResponseEntity.ok(commits);
    }
}
