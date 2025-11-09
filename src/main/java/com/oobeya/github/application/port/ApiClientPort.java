package com.oobeya.github.application.port;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.RepoDto;

import java.util.List;

public interface ApiClientPort {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
    List<RepoInitrequest> getUserRepos(String accessToken);
    List<CommitDto> getCommits(String accessToken, String owner, String repo);
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);

    List<RepoDto> getOrgRepos(String accessToken, String orgLogin);
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}
