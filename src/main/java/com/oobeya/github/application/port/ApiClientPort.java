package com.oobeya.github.application.port;

import java.util.List;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.response.LanguageDto;

public interface ApiClientPort {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
    List<RepoInitrequest> getUserRepos(String accessToken);
    List<CommitDto> getCommits(String accessToken, String owner, String repo, String branch, String since);
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}
