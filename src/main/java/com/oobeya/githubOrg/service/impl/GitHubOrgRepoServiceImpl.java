package com.oobeya.githubOrg.service.impl;

import com.oobeya.auth.entity.User;
import com.oobeya.auth.repository.UserRepository;
import com.oobeya.auth.security.jwtservice.JwtService;
import com.oobeya.github.dto.RepoDto;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.githubOrg.dto.BranchDto;
import com.oobeya.githubOrg.service.GitHubOrgRepoService;
import com.oobeya.github.util.GitHubUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubOrgRepoServiceImpl implements GitHubOrgRepoService {

    private final WebClient githubWebClient; // ✅ doğru kullanım
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public List<RepoDto> getOrganizationRepos(String jwtToken, String organization) {
        String login = jwtService.getLoginFromToken(jwtToken);
        String accessToken = userRepository.findByLogin(login)
                .map(User::getAccessToken)
                .orElseThrow(() -> new GitHubApiException("User not found: " + login));

        String endpoint = String.format("https://api.github.com/orgs/%s/repos", organization);

        return githubWebClient.get()
                .uri(endpoint)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class).map(GitHubApiException::new))
                .bodyToFlux(RepoDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<BranchDto> getBranchesOfOrganizationRepo(String jwtToken, String organization, String repoName) {
        String login = jwtService.getLoginFromToken(jwtToken);
        String accessToken = userRepository.findByLogin(login)
                .map(User::getAccessToken)
                .orElseThrow(() -> new GitHubApiException("User not found: " + login));

        String url = String.format("https://api.github.com/repos/%s/%s/branches", organization, repoName);

        try {
            BranchDto[] branches = githubWebClient.get() // ✅ düzeltildi
                    .uri(url)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(BranchDto[].class)
                    .block();

            return branches != null ? Arrays.asList(branches) : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("Failed to fetch branches: {}", e.getMessage());
            throw new GitHubApiException("Failed to fetch branches from GitHub");
        }
    }
}
