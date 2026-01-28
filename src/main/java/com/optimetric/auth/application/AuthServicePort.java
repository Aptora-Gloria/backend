package com.optimetric.auth.application;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.application.dto.UserInfo;

import java.util.Optional;

/**
 * Application port for authentication operations.
 * This port uses Application DTOs, not API DTOs.
 * 
 * Note: This port will be replaced by Use Cases in PHASE 4.
 * For now, it's updated to use Application DTOs instead of API DTOs.
 */
public interface AuthServicePort {

    /**
     * Performs user login with GitHub OAuth code.
     * 
     * @param command Login command containing OAuth code
     * @return Login result containing JWT access token
     */
    LoginResult login(LoginCommand command);

    /**
     * Gets current user information from JWT token.
     * 
     * @param token JWT token
     * @return User information
     */
    UserInfo getCurrentUser(String token);

    /**
     * Finds user by login.
     * 
     * @param login User login
     * @return Optional user information
     */
    Optional<UserInfo> findByLogin(String login);

    /**
     * Extracts login from JWT token.
     * 
     * @param token JWT token
     * @return User login
     */
    String getLoginFromToken(String token);

    /**
     * Gets GitHub access token from JWT token.
     * 
     * @param jwtToken JWT token
     * @return GitHub access token
     */
    String getAccessTokenFromToken(String jwtToken);

    /**
     * Gets GitHub access token from user login.
     * 
     * @param login User login
     * @return GitHub access token
     */
    String getAccessTokenFromLogin(String login);
    
    /**
     * Gets user by login or throws UserNotFoundException.
     * This method should be used when user existence is required.
     * 
     * @param login User login
     * @return User information
     * @throws com.optimetric.shared.exception.UserNotFoundException if user not found
     */
    UserInfo findByLoginOrThrow(String login);
}
