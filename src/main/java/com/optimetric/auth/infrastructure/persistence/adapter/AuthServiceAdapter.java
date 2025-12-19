package com.optimetric.auth.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.infrastructure.core.mapper.GitHubUserMapper;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.optimetric.auth.application.TokenPort;
import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.shared.exception.GitHubApiException;
import com.optimetric.shared.exception.UserNotFoundException;
import com.optimetric.github.application.port.GitHubServicePort;
import com.optimetric.github.infrastructure.util.TokenUtil;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthServiceAdapter implements AuthServicePort {

    private final UserRepository userRepository;
    private final TokenPort tokenPort;
    private final GitHubServicePort githubService;


    @Override
    public AuthResponse login(AuthRequest request) {
        String accessToken = fetchAccessToken(request.code());
        GitHubUserDto userDto = fetchUserInfo(accessToken);
        User user = saveOrUpdateUser(userDto, accessToken);
        String jwt = tokenPort.createToken(user.getLogin());
        return new AuthResponse(jwt);
    }

    private String fetchAccessToken(String code) {
        GitHubAccessTokenResponse tokenResponse;
        try {
            tokenResponse = githubService.getAccessTokenFromCode(code);
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to exchange authorization code for access token: " + e.getMessage(), e);
        }
        
        if (tokenResponse == null) {
            throw new GitHubApiException("GitHub access token response is null. The authorization code may be invalid or expired.");
        }
        
        String accessToken = tokenResponse.accessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new GitHubApiException("GitHub access token is null or empty in the response.");
        }
        return accessToken;
    }

    private GitHubUserDto fetchUserInfo(String accessToken) {
        GitHubUserDto apiUser;
        try {
            apiUser = githubService.getUserInfo(accessToken);
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to fetch user info from GitHub: " + e.getMessage(), e);
        }

        // DTO'ya accessToken ekle (record immutable olduğu için yeni örnek)
        return new GitHubUserDto(
                apiUser.id(),
                apiUser.githubUserId(),
                apiUser.login(),
                accessToken,
                apiUser.name(),
                apiUser.avatar_url(),
                apiUser.htmlUrl(),
                apiUser.ownerType(),
                apiUser.lastSyncedAt()
        );
    }

    private User saveOrUpdateUser(GitHubUserDto dto, String accessToken) {
        User entity = userRepository.findByGithubUserId(dto.githubUserId())
                .orElseGet(() -> userRepository.findByLogin(dto.login()).orElse(null));

        if (entity == null) {
            entity = GitHubUserMapper.toNewEntity(dto);
        } else {
            GitHubUserMapper.updateEntityFromDto(dto, entity);
            entity.setAccessToken(accessToken);
        }

        return userRepository.save(entity);
    }



    @Override
    public GitHubUserDto getCurrentUser(String token) {
        String login = tokenPort.getLoginFromToken(token);

        User user = findByLoginOrThrow(login);
        return GitHubUserMapper.toDto(user);
    }


    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public String getLoginFromToken(String token) {
        return tokenPort.getLoginFromToken(token);
    }

    @Override
    public String getAccessTokenFromToken(String jwtToken) {
        String extractToken = TokenUtil.extractToken(jwtToken);
        String login = getLoginFromToken(extractToken);
        User user = findByLoginOrThrow(login);
        return user.getAccessToken();
    }

    @Override
    public String getAccessTokenFromLogin(String login) {
        User user = findByLoginOrThrow(login);
        return user.getAccessToken();
    }

    @Override
    public User findByLoginOrThrow(String login) throws UserNotFoundException {
        return findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
    }
}
