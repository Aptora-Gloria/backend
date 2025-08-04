package com.oobeya.githubOrg.service.impl;

import com.oobeya.auth.entity.User;
import com.oobeya.auth.repository.UserRepository;
import com.oobeya.auth.security.jwtservice.JwtService;
import com.oobeya.github.constant.GitHubConstants;
import com.oobeya.githubOrg.dto.GitHubOrganizationDto;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.githubOrg.service.GitHubOrganizationService;
import com.oobeya.github.util.GitHubUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubOrganizationServiceImpl implements GitHubOrganizationService {

    private final WebClient githubWebClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public List<GitHubOrganizationDto> getUserOrganizations(String jwtToken) {
        String login = jwtService.getLoginFromToken(jwtToken);

        String accessToken = userRepository.findByLogin(login)
                .map(User::getAccessToken)
                .orElseThrow(() -> new GitHubApiException(GitHubConstants.ERROR_USER_NOT_FOUND + login));

        return githubWebClient.get()
                .uri(GitHubConstants.USER_ORGS_ENDPOINT) // örn: "https://api.github.com/user/orgs"
                .headers(headers -> {
                    headers.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken));
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).map(errorBody ->
                                new GitHubApiException(GitHubConstants.ERROR_ORG_FETCH_FAILED + errorBody)
                        )
                )
                .bodyToFlux(GitHubOrganizationDto.class)
                .collectList()
                .block();
    }
}
