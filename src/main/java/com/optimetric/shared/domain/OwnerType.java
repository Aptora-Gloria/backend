package com.optimetric.shared.domain;

/**
 * Domain enum for repository owner type.
 * This is a shared domain concept, not tied to any infrastructure layer.
 */
public enum OwnerType {
    USER,
    ORGANIZATION;

    /**
     * Creates OwnerType from GitHub API type string.
     * 
     * @param githubType GitHub owner type string ("user" or "organization")
     * @return OwnerType enum value
     * @throws IllegalArgumentException if githubType is unknown
     */
    public static OwnerType fromGitHubType(String githubType) {
        if (githubType == null) return null;
        return switch (githubType.toLowerCase()) {
            case "user" -> USER;
            case "organization" -> ORGANIZATION;
            default -> throw new IllegalArgumentException("Unknown GitHub owner type: " + githubType);
        };
    }
}

