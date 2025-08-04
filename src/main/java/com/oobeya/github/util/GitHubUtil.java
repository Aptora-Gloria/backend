package com.oobeya.github.util;

import java.nio.charset.StandardCharsets; // Bu satırı ekleyin
import java.util.Base64; // Bu satırı ekleyin

public class GitHubUtil {
    private GitHubUtil() {
        // Utility class, instantiation not allowed
    }
    /**
     * Builds a complete "Bearer" Authorization header value from token.
     * @param token GitHub access token
     * @return "Bearer <token>"
     */
    public static String buildBearerHeader(String token) {
        return "Bearer " + token;
    }
    /**
     * Decodes a Base64 encoded string content.
     * @param content The Base64 encoded string.
     * @param encoding The encoding type (e.g., "base64").
     * @return The decoded string content.
     */
    public static String decodeBase64(String content, String encoding) {
        if ("base64".equals(encoding)) {
            // MIME Decoder, RFC 2045 uyumlu Base64 verilerini işler
            byte[] decodedBytes = Base64.getMimeDecoder().decode(content);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }
        // Eğer encoding base64 değilse veya bilinmiyorsa, içeriği olduğu gibi döndür.
        return content;
    }
}