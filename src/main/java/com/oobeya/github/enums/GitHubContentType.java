package com.oobeya.github.enums;

public enum GitHubContentType {
    FILE,
    DIRECTORY;

    public static GitHubContentType fromString(String type) {
        return switch (type.toLowerCase()) {
            case "file" -> FILE;
            case "dir" -> DIRECTORY;
            default -> throw new IllegalArgumentException("Unknown content type: " + type);
        };
    }
}
