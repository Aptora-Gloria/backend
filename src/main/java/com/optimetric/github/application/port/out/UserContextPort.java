package com.optimetric.github.application.port.out;

import java.util.Optional;

import com.optimetric.github.application.dto.UserContext;

/**
 * Outbound port for retrieving user context.
 * Provides abstraction for user-related operations, avoiding direct dependency
 * on auth service ports in the application layer.
 * 
 * Note: Outbound ports should not throw exceptions. Exceptions are thrown
 * by infrastructure adapters and handled by use cases.
 */
public interface UserContextPort {
    /**
     * Gets user context by login.
     * Returns minimal user information including userId, login, and access token.
     *
     * @param login User login
     * @return Optional UserContext containing userId, login, and accessToken.
     *         Empty if user not found (exception handling is done in adapter/use case).
     */
    Optional<UserContext> getContextByLogin(String login);
}
