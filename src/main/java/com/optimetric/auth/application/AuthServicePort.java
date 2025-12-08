package com.optimetric.auth.application;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.Optional;

public interface AuthServicePort {

    AuthResponse login(AuthRequest request);

    GitHubUserDto getCurrentUser(String token);

    Optional<User> findByLogin(String login);

    String getLoginFromToken(String token);

    String getAccessTokenFromToken(String authToken);

    String getAccessTokenFromLogin(String login);
    
    /**
     * Gets user by login or throws UserNotFoundException.
     * This method should be used when user existence is required.
     * 
     * @param login User login
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    User findByLoginOrThrow(String login) throws UserNotFoundException;
}
