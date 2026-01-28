package com.optimetric.auth.infrastructure.persistence.adapter;

import org.springframework.stereotype.Service;

import com.optimetric.auth.domain.service.AuthenticationDomainService;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.github.application.dto.UserContext;
import com.optimetric.github.application.port.out.UserContextPort;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure adapter for UserContextPort.
 * Implements user context retrieval using AuthenticationDomainService.
 */
@Service
@RequiredArgsConstructor
public class UserContextAdapter implements UserContextPort {

    private final AuthenticationDomainService authenticationDomainService;

    @Override
    public java.util.Optional<UserContext> getContextByLogin(String login) {
        try {
            UserInfo userInfo = authenticationDomainService.findByLoginOrThrow(login);
            String accessToken = authenticationDomainService.getAccessTokenFromLogin(login);
            
            return java.util.Optional.of(new UserContext(
                    userInfo.id(),
                    userInfo.login(),
                    accessToken
            ));
        } catch (com.optimetric.shared.exception.UserNotFoundException e) {
            // User not found - return empty Optional
            // Exception handling is done in use case layer
            return java.util.Optional.empty();
        }
    }
}
