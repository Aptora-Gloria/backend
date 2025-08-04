package com.oobeya.github.util;

public class TokenUtil {

    private TokenUtil() {
        // Utility class – no instantiation
    }

    /**
     * Extracts the JWT token from an Authorization header.
     * Example: "Bearer eyJhbGciOiJIUzI1NiIsInR..." → "eyJhbGciOiJIUzI1NiIsInR..."
     */
    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
