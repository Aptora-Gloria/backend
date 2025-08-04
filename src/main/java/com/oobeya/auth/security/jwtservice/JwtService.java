package com.oobeya.auth.security.jwtservice;


public interface JwtService {
    String createToken(String login);
    String getLoginFromToken(String token);
}
