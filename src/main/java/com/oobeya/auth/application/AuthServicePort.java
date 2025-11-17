package com.oobeya.auth.application;

import com.oobeya.auth.api.dto.AuthRequest;
import com.oobeya.auth.api.dto.AuthResponse;
import com.oobeya.auth.infrastructure.persistence.entity.User;
import com.oobeya.auth.api.dto.GitHubUserDto;

import java.util.Optional;

public interface AuthServicePort {


    AuthResponse login(AuthRequest request);


    GitHubUserDto getCurrentUser(String token);

    Optional<User> findByLogin(String login);

    String getLoginFromToken(String token);

    String getAccessTokenFromToken(String authToken);

    String getAccessTokenFromLogin(String login);


}
