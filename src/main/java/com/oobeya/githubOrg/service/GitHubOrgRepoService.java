package com.oobeya.githubOrg.service;

import com.oobeya.github.dto.response.RepoDto;
import com.oobeya.githubOrg.dto.BranchDto;

import java.util.List;

public interface GitHubOrgRepoService {
    List<RepoDto> getOrganizationRepos(String jwtToken, String organization);
    List<BranchDto> getBranchesOfOrganizationRepo(String token, String organization, String repoName);


}
