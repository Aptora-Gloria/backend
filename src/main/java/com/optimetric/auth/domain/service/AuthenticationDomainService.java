package com.optimetric.auth.domain.service;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.application.port.out.GitHubOAuthPort;
import com.optimetric.auth.application.port.out.JwtTokenPort;
import com.optimetric.auth.application.port.out.UserPersistencePort;
import com.optimetric.auth.domain.model.User;
import com.optimetric.shared.exception.GitHubApiException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Kimlik doğrulama iş kuralları ve orkestrasyonu.
@Service
@RequiredArgsConstructor
public class AuthenticationDomainService {

    private final GitHubOAuthPort githubOAuthPort;
    private final UserPersistencePort userPersistencePort;
    private final JwtTokenPort jwtTokenPort;

    // GitHub OAuth kodu ile giriş: token al, kullanıcı bilgisi al, kaydet, JWT üret.
    public String login(String code) {
        String accessToken = exchangeCodeForToken(code);
        GitHubUserDto githubUser = fetchUserInfo(accessToken);
        User user = saveOrUpdateUser(githubUser, accessToken);
        return jwtTokenPort.createToken(user.getLogin());
    }

    // JWT token ile mevcut kullanıcı bilgisini döner.
    public UserInfo getCurrentUser(String token) {
        String login = jwtTokenPort.extractLoginFromToken(token);
        return userPersistencePort.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
    }

    // Login ile kullanıcı arar; yoksa boş döner.
    public Optional<UserInfo> findByLogin(String login) {
        return userPersistencePort.findByLogin(login);
    }

    // Login ile kullanıcı arar; yoksa UserNotFoundException fırlatır.
    public UserInfo findByLoginOrThrow(String login) {
        return userPersistencePort.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
    }

    // Login ile kullanıcının GitHub access token'ını döner.
    public String getAccessTokenFromLogin(String login) {
        UserInfo userInfo = findByLoginOrThrow(login);
        return userInfo.accessToken();
    }

    // Dahili id ile kullanıcı arar; diğer modüller için.
    public Optional<UserInfo> findById(Long id) {
        return userPersistencePort.findById(id);
    }

    private String exchangeCodeForToken(String code) {
        GitHubAccessTokenResponse tokenResponse;
        try {
            tokenResponse = githubOAuthPort.exchangeCodeForToken(code);
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
        try {
            return githubOAuthPort.fetchUserInfo(accessToken);
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to fetch user info from GitHub: " + e.getMessage(), e);
        }
    }

    private User saveOrUpdateUser(GitHubUserDto githubUser, String accessToken) {
        // Try to find existing user by GitHub user ID or login
        Optional<UserInfo> existingUser = userPersistencePort.findByGithubUserId(githubUser.githubUserId())
                .or(() -> userPersistencePort.findByLogin(githubUser.login()));

        User user;
        if (existingUser.isPresent()) {
            UserInfo userInfo = existingUser.get();
            user = User.fromExisting(
                    userInfo.id(),
                    userInfo.githubUserId(),
                    userInfo.login(),
                    accessToken,
                    githubUser.name(),
                    githubUser.avatar_url(),
                    githubUser.htmlUrl(),
                    githubUser.ownerType() != null ? 
                            com.optimetric.shared.domain.OwnerType.fromGitHubType(githubUser.ownerType()) : 
                            com.optimetric.shared.domain.OwnerType.USER,
                    userInfo.lastSyncedAt()
            );
        } else {
            user = User.fromGitHubData(
                    githubUser.githubUserId(),
                    githubUser.login(),
                    accessToken,
                    githubUser.name(),
                    githubUser.avatar_url(),
                    githubUser.htmlUrl(),
                    githubUser.ownerType() != null ? 
                            com.optimetric.shared.domain.OwnerType.fromGitHubType(githubUser.ownerType()) : 
                            com.optimetric.shared.domain.OwnerType.USER
            );
        }

        UserInfo userInfo = toUserInfo(user);
        UserInfo savedUserInfo = userPersistencePort.save(userInfo);
        
        // Return updated user domain model
        return User.fromExisting(
                savedUserInfo.id(),
                savedUserInfo.githubUserId(),
                savedUserInfo.login(),
                savedUserInfo.accessToken(),
                savedUserInfo.name(),
                savedUserInfo.avatarUrl(),
                savedUserInfo.htmlUrl(),
                savedUserInfo.ownerType(),
                savedUserInfo.lastSyncedAt()
        );
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getGithubUserId(),
                user.getLogin(),
                user.getAccessToken(),
                user.getName(),
                user.getAvatarUrl(),
                user.getHtmlUrl(),
                user.getOwnerType(),
                user.getLastSyncedAt()
        );
    }
}
