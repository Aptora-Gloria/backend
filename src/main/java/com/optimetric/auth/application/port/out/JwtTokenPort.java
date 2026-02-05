package com.optimetric.auth.application.port.out;

public interface JwtTokenPort {

    String createToken(String login);

    String extractLoginFromToken(String token);
}
