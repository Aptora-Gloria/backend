package com.oobeya.githubOrg.controller;

import com.oobeya.githubOrg.dto.GitHubOrganizationDto;
import com.oobeya.githubOrg.service.GitHubOrganizationService;
import com.oobeya.github.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/github/orgs")
@RequiredArgsConstructor
public class GitHubOrganizationController {

    private final GitHubOrganizationService gitHubOrganizationService;

    @GetMapping
    public ResponseEntity<List<GitHubOrganizationDto>> getUserOrganizations(
            @RequestHeader("Authorization") String authHeader
    ) {
        String jwtToken = TokenUtil.extractToken(authHeader);
        List<GitHubOrganizationDto> orgs = gitHubOrganizationService.getUserOrganizations(jwtToken);
        return ResponseEntity.ok(orgs);
    }
}

