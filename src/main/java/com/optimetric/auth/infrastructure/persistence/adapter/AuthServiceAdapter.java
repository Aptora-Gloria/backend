package com.optimetric.auth.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.domain.service.AuthenticationDomainService;
import com.optimetric.auth.application.port.out.JwtTokenPort;
import com.optimetric.github.infrastructure.util.TokenUtil;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure adapter for AuthServicePort.
 * This is a facade adapter that delegates to AuthenticationDomainService.
 * Kept for backward compatibility with other modules.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceAdapter implements AuthServicePort {

    private final AuthenticationDomainService authenticationDomainService;
    private final JwtTokenPort jwtTokenPort;

    @Override
    public LoginResult login(LoginCommand command) {
        String jwtToken = authenticationDomainService.login(command.code());
        return new LoginResult(jwtToken);
    }

    @Override
    public UserInfo getCurrentUser(String token) {
        return authenticationDomainService.getCurrentUser(token);
    }

    @Override
    public Optional<UserInfo> findByLogin(String login) {
        return authenticationDomainService.findByLogin(login);
    }

    @Override
    public String getLoginFromToken(String token) {
        return jwtTokenPort.extractLoginFromToken(token);
    }

    @Override
    public String getAccessTokenFromToken(String jwtToken) {
        String extractToken = TokenUtil.extractToken(jwtToken);
        String login = getLoginFromToken(extractToken);
        UserInfo userInfo = authenticationDomainService.findByLoginOrThrow(login);
        return userInfo.accessToken();
    }

    @Override
    public String getAccessTokenFromLogin(String login) {
        return authenticationDomainService.getAccessTokenFromLogin(login);
    }

    @Override
    public UserInfo findByLoginOrThrow(String login) {
        return authenticationDomainService.findByLoginOrThrow(login);
    }
}
