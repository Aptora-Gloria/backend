package com.oobeya.github.service;

import com.oobeya.github.dto.*;

import java.util.List;

public interface ApiClientI {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
    List<RepoDto> getUserRepos(String accessToken);
    List<ContentDto> getRepoContents(String accessToken, String owner, String repo, String path);
    List<CommitDto> getCommits(String accessToken, String owner, String repo);
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);
    List<ContributorStatsDto> fetchContributorStats(String accessToken, String owner, String repo);

}
