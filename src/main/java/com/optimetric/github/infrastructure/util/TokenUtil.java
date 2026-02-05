package com.optimetric.github.infrastructure.util;

public class TokenUtil {

    private TokenUtil() {
        // Utility class – no instantiation
    }

    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
