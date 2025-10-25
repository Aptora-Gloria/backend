package com.oobeya.auth.service;

import com.oobeya.auth.dto.AuthRequest;
import com.oobeya.auth.dto.AuthResponse;
import com.oobeya.auth.entity.User;
import com.oobeya.auth.dto.GitHubUserDto;

import java.util.Optional;

public interface AuthService {


    AuthResponse login(AuthRequest request);


    GitHubUserDto getCurrentUser(String token);

    Optional<User> findByLogin(String login);

    String getLoginFromToken(String token);

    String getAccessTokenFromToken(String authToken);


}
