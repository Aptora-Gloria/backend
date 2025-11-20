package com.optimetric.auth.application;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.api.dto.GitHubUserDto;

import java.util.Optional;

public interface AuthServicePort {


    AuthResponse login(AuthRequest request);


    GitHubUserDto getCurrentUser(String token);

    Optional<User> findByLogin(String login);

    String getLoginFromToken(String token);

    String getAccessTokenFromToken(String authToken);

    String getAccessTokenFromLogin(String login);


}
