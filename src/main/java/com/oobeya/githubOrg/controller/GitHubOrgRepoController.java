package com.oobeya.githubOrg.controller;

import com.oobeya.github.dto.RepoDto;
import com.oobeya.githubOrg.dto.BranchDto;
import com.oobeya.githubOrg.service.GitHubOrgRepoService;
import com.oobeya.github.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/github/orgs/repos")
@RequiredArgsConstructor
public class GitHubOrgRepoController {

    private final GitHubOrgRepoService gitHubOrgRepoService;

    @GetMapping("/{org}")
    public ResponseEntity<List<RepoDto>> getOrganizationRepos(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("org") String organization) {

        String jwtToken = TokenUtil.extractToken(authHeader);
        List<RepoDto> repos = gitHubOrgRepoService.getOrganizationRepos(jwtToken, organization);
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/{org}/{repoName}/branches")
    public ResponseEntity<List<BranchDto>> getRepoBranches(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("org") String organization,
            @PathVariable("repoName") String repoName) {

        String jwtToken = TokenUtil.extractToken(authHeader);
        List<BranchDto> branches = gitHubOrgRepoService.getBranchesOfOrganizationRepo(jwtToken, organization, repoName);
        return ResponseEntity.ok(branches);
    }
}


