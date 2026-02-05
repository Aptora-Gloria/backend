package com.optimetric.github.domain.vo;

import java.util.Objects;

public final class RepositoryFullName {
    
    private static final String FULL_NAME_PATTERN = "^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$";
    private static final int MAX_LENGTH = 200; // GitHub repo adı için makul üst sınır
    
    private final String value;
    
    public RepositoryFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Repository full name cannot be null or blank");
        }
        
        String trimmed = fullName.trim();
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Repository full name cannot exceed %d characters", MAX_LENGTH));
        }
        
        if (!trimmed.matches(FULL_NAME_PATTERN)) {
            throw new IllegalArgumentException(
                "Repository full name must be in 'owner/repo' format. " +
                "Owner and repo names can only contain alphanumeric characters, dots, underscores, and hyphens.");
        }
        
        // Additional validation: must contain exactly one slash
        int slashCount = (int) trimmed.chars().filter(ch -> ch == '/').count();
        if (slashCount != 1) {
            throw new IllegalArgumentException(
                "Repository full name must contain exactly one slash separating owner and repository name");
        }
        
        // owner ve repo parçalarının boş olmadığını doğrula
        String[] parts = trimmed.split("/");
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException(
                "Repository full name must have non-empty owner and repository name parts");
        }
        
        this.value = trimmed;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getOwner() {
        return value.split("/")[0];
    }
    
    public String getRepoName() {
        return value.split("/")[1];
    }
    
    public static RepositoryFullName ofNullable(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        try {
            return new RepositoryFullName(fullName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static boolean isValid(String fullName) {
        try {
            new RepositoryFullName(fullName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryFullName that = (RepositoryFullName) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
