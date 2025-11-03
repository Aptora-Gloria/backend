package com.oobeya.github.service;

import com.oobeya.auth.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.dto.GitHubUserDto;
import com.oobeya.github.dto.request.RepoInitrequest;
import com.oobeya.github.dto.response.*;

import java.util.List;

public interface ApiClientI {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
    List<RepoInitrequest> getUserRepos(String accessToken);
    List<CommitDto> getCommits(String accessToken, String owner, String repo);
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);

    List<RepoDto> getOrgRepos(String accessToken, String orgLogin);
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}
