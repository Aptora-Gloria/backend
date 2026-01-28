package com.optimetric.auth.domain.model;

import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;

/**
 * Domain model for User.
 * This is a domain entity that contains business logic and validation rules.
 */
public class User {

    private Long id;
    private Long githubUserId;
    private String login;
    private String accessToken;
    private String name;
    private String avatarUrl;
    private String htmlUrl;
    private OwnerType ownerType;
    private OffsetDateTime lastSyncedAt;

    // Private constructor to enforce creation through factory methods
    private User() {}

    /**
     * Factory method to create a new user from GitHub data.
     */
    public static User fromGitHubData(
            Long githubUserId,
            String login,
            String accessToken,
            String name,
            String avatarUrl,
            String htmlUrl,
            OwnerType ownerType) {
        
        User user = new User();
        user.githubUserId = githubUserId;
        user.login = validateLogin(login);
        user.accessToken = accessToken;
        user.name = name;
        user.avatarUrl = avatarUrl;
        user.htmlUrl = htmlUrl;
        user.ownerType = ownerType != null ? ownerType : OwnerType.USER;
        user.lastSyncedAt = OffsetDateTime.now();
        return user;
    }

    /**
     * Factory method to create a user from existing data (for updates).
     */
    public static User fromExisting(
            Long id,
            Long githubUserId,
            String login,
            String accessToken,
            String name,
            String avatarUrl,
            String htmlUrl,
            OwnerType ownerType,
            OffsetDateTime lastSyncedAt) {
        
        User user = new User();
        user.id = id;
        user.githubUserId = githubUserId;
        user.login = validateLogin(login);
        user.accessToken = accessToken;
        user.name = name;
        user.avatarUrl = avatarUrl;
        user.htmlUrl = htmlUrl;
        user.ownerType = ownerType != null ? ownerType : OwnerType.USER;
        user.lastSyncedAt = lastSyncedAt != null ? lastSyncedAt : OffsetDateTime.now();
        return user;
    }

    /**
     * Updates user information from GitHub data.
     */
    public void updateFromGitHubData(
            String accessToken,
            String name,
            String avatarUrl,
            String htmlUrl,
            OwnerType ownerType) {
        
        this.accessToken = accessToken;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.ownerType = ownerType != null ? ownerType : OwnerType.USER;
        this.lastSyncedAt = OffsetDateTime.now();
    }

    /**
     * Validates login format (business rule).
     */
    private static String validateLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be null or empty");
        }
        if (login.length() > 100) {
            throw new IllegalArgumentException("Login cannot exceed 100 characters");
        }
        return login.trim();
    }

    // Getters
    public Long getId() { return id; }
    public Long getGithubUserId() { return githubUserId; }
    public String getLogin() { return login; }
    public String getAccessToken() { return accessToken; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getHtmlUrl() { return htmlUrl; }
    public OwnerType getOwnerType() { return ownerType; }
    public OffsetDateTime getLastSyncedAt() { return lastSyncedAt; }
}
