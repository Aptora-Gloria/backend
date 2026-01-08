package com.optimetric.github.application.port;

import com.optimetric.github.application.dto.UserContext;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Outbound port for retrieving user context.
 * Provides abstraction for user-related operations, avoiding direct dependency
 * on auth service ports in the application layer.
 */
public interface UserContextPort {
    /**
     * Gets user context by login.
     * Returns minimal user information including userId, login, and access token.
     *
     * @param login User login
     * @return UserContext containing userId, login, and accessToken
     * @throws UserNotFoundException if user not found
     */
    UserContext getContextByLogin(String login) throws UserNotFoundException;
}
