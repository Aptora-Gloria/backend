package com.oobeya.github.application.port;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.GitHubPullRequestResponse;
import com.oobeya.github.dto.response.PullRequestDto;

import java.util.List;

public interface ApiClientPort {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
    List<RepoInitrequest> getUserRepos(String accessToken);
    List<CommitDto> getCommits(String accessToken, String owner, String repo);
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
    
    // Pull Request methods
    List<PullRequestDto> getPullRequests(String accessToken, String owner, String repo, String state, Integer page, Integer perPage);
    PullRequestDto getPullRequestByNumber(String accessToken, String owner, String repo, Integer pullRequestNumber);
    
    // Pull Request methods returning raw GitHub response (for branch mapping)
    List<GitHubPullRequestResponse> getPullRequestsRaw(String accessToken, String owner, String repo, String state, Integer page, Integer perPage);
    GitHubPullRequestResponse getPullRequestByNumberRaw(String accessToken, String owner, String repo, Integer pullRequestNumber);
}
