package com.optimetric.shared.domain;

public enum OwnerType {
    USER,
    ORGANIZATION;

    public static OwnerType fromGitHubType(String githubType) {
        if (githubType == null) return null;
        return switch (githubType.toLowerCase()) {
            case "user" -> USER;
            case "organization" -> ORGANIZATION;
            default -> throw new IllegalArgumentException("Unknown GitHub owner type: " + githubType);
        };
    }
}

