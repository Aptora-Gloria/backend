package com.oobeya.githubOrg.service.impl;



import com.oobeya.auth.entity.User;
import com.oobeya.auth.repository.UserRepository;
import com.oobeya.auth.security.jwtservice.JwtService;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.github.util.GitHubUtil;
import com.oobeya.githubOrg.dto.OrgCommitDto;
import com.oobeya.githubOrg.service.GitHubOrgCommitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubOrgCommitServiceImpl implements GitHubOrgCommitService {

    private final WebClient githubWebClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public List<OrgCommitDto> getCommitsOfOrgRepoBranch(String jwtToken, String org, String repo, String branch) {
        String login = jwtService.getLoginFromToken(jwtToken);
        String accessToken = userRepository.findByLogin(login)
                .map(User::getAccessToken)
                .orElseThrow(() -> new GitHubApiException("User not found: " + login));

        String endpoint = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s", org, repo, branch);

        try {
            OrgCommitDto[] commits = githubWebClient.get()
                    .uri(endpoint)
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class).map(GitHubApiException::new))
                    .bodyToMono(OrgCommitDto[].class)
                    .block();

            return commits != null ? Arrays.asList(commits) : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch commits for org repo branch: {}", e.getMessage());
            throw new GitHubApiException("Error while fetching commits from GitHub");
        }
    }
}
