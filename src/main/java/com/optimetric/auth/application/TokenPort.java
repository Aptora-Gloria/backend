package com.optimetric.auth.application;


public interface TokenPort {
    String createToken(String login);
    String getLoginFromToken(String token);
}
