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
    public UserContext getContextByLogin(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        
        return new UserContext(
                user.getId(),
                user.getLogin(),
                accessToken
        );
    }
}
