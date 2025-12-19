package com.optimetric.auth.infrastructure.persistence.entity;

public enum OwnerType {
    USER,
    ORGANIZATION;

    public static OwnerType fromGitHubType(String githubType) {
        return switch (githubType.toLowerCase()) {
            case "user" -> USER;
            case "organization" -> ORGANIZATION;
            default -> throw new IllegalArgumentException("Unknown GitHub owner type: " + githubType);
        };
    }
}

