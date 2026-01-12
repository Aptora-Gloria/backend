package com.optimetric.auth.infrastructure.persistence.adapter;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.UserContext;
import com.optimetric.github.application.port.out.UserContextPort;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure adapter for UserContextPort.
 * Implements user context retrieval using AuthServicePort.
 */
@Service
@RequiredArgsConstructor
public class UserContextAdapter implements UserContextPort {

    private final AuthServicePort authServicePort;

    @Override
    public java.util.Optional<UserContext> getContextByLogin(String login) {
        try {
            User user = authServicePort.findByLoginOrThrow(login);
            String accessToken = authServicePort.getAccessTokenFromLogin(login);
            
            return java.util.Optional.of(new UserContext(
                    user.getId(),
                    user.getLogin(),
                    accessToken
            ));
        } catch (com.optimetric.shared.exception.UserNotFoundException e) {
            // User not found - return empty Optional
            // Exception handling is done in use case layer
            return java.util.Optional.empty();
        }
    }
}
