package com.optimetric.auth.application.port.out;

/**
 * Outbound port for JWT token operations.
 * This port abstracts JWT token generation and parsing from the application layer.
 */
public interface JwtTokenPort {

    /**
     * Creates a JWT token for a user login.
     * 
     * @param login User login
     * @return JWT token string
     */
    String createToken(String login);

    /**
     * Extracts user login from JWT token.
     * 
     * @param token JWT token string
     * @return User login
     * @throws com.optimetric.shared.exception.InvalidTokenException if token is invalid
     */
    String extractLoginFromToken(String token);
}
