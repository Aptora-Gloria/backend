package com.oobeya.githubOrg.service;

import com.oobeya.githubOrg.dto.GitHubOrganizationDto;

import java.util.List;

public interface GitHubOrganizationService {
    List<GitHubOrganizationDto> getUserOrganizations(String jwtToken);
}
