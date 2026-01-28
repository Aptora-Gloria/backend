package com.optimetric.auth.application.port.out;

import com.optimetric.auth.application.dto.UserInfo;

import java.util.Optional;

/**
 * Outbound port for user persistence operations.
 * This port abstracts database access from the application layer.
 */
public interface UserPersistencePort {

    /**
     * Saves or updates a user.
     * 
     * @param userInfo User information to save or update
     * @return Saved user information
     */
    UserInfo save(UserInfo userInfo);

    /**
     * Finds user by login.
     * 
     * @param login User login
     * @return Optional user information
     */
    Optional<UserInfo> findByLogin(String login);

    /**
     * Finds user by GitHub user ID.
     * 
     * @param githubUserId GitHub user ID
     * @return Optional user information
     */
    Optional<UserInfo> findByGithubUserId(Long githubUserId);
}
