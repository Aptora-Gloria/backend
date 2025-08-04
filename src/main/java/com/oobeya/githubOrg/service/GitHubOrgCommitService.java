package com.oobeya.githubOrg.service;


import com.oobeya.githubOrg.dto.OrgCommitDto;

import java.util.List;

public interface GitHubOrgCommitService {
    List<OrgCommitDto> getCommitsOfOrgRepoBranch(String jwtToken, String org, String repo, String branch);
}

