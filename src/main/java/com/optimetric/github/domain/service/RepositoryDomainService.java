package com.optimetric.github.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.domain.vo.RepositoryFullName;
import com.optimetric.shared.domain.OwnerType;

/**
 * Domain service for repository business rules and validations.
 * 
 * This service contains pure business logic that doesn't depend on external systems.
 * It operates on domain objects and enforces business rules.
 * 
 * Responsibilities:
 * - Repository access validation
 * - Repository filtering logic
 * - Business rule enforcement
 * - Domain calculations
 * 
 * Note: This is a pure domain service with no framework dependencies.
 * It is registered as a Spring bean via configuration class for DI purposes.
 */
public class RepositoryDomainService {

    /**
     * Validates if a repository full name is in correct format.
     * Uses RepositoryFullName value object for validation.
     * 
     * @param fullName Repository full name string
     * @return true if valid, false otherwise
     */
    public boolean isValidFullName(String fullName) {
        return RepositoryFullName.isValid(fullName);
    }

    /**
     * Filters repositories by owner type and owner login.
     * Pure business logic - no external dependencies.
     * 
     * @param repositories List of repositories to filter
     * @param ownerType Owner type filter (optional)
     * @param ownerLogin Owner login filter (optional)
     * @return Filtered list of repositories
     */
    public List<RepositoryInfo> filterByCriteria(
            List<RepositoryInfo> repositories,
            OwnerType ownerType,
            String ownerLogin) {
        
        return repositories.stream()
                .filter(repo -> {
                    // Filter by owner type if specified
                    if (ownerType != null && repo.ownerType() != ownerType) {
                        return false;
                    }
                    
                    // Filter by owner login if specified
                    if (ownerLogin != null && !ownerLogin.equals(repo.ownerLogin())) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Validates if a user can access a repository based on business rules.
     * This is a domain-level validation (e.g., private repo access rules).
     * 
     * Note: This method doesn't check database permissions - that's infrastructure concern.
     * This checks business rules like "user can only access their own private repos".
     * 
     * @param userLogin User login
     * @param repository Repository to check
     * @return true if user can access based on business rules
     */
    public boolean canUserAccessRepository(String userLogin, RepositoryInfo repository) {
        // Business rule: User can access their own repositories
        if (userLogin.equals(repository.ownerLogin())) {
            return true;
        }
        
        // Business rule: Public repositories are accessible to everyone
        if (Boolean.FALSE.equals(repository.isPrivate())) {
            return true;
        }
        
        // Business rule: Private repositories are only accessible to owner
        // (This is a simplified rule - actual access control might be more complex)
        return false;
    }

    /**
     * Checks if repository should be included based on visibility rules.
     * 
     * @param repository Repository to check
     * @param includePrivate Whether to include private repositories
     * @return true if repository should be included
     */
    public boolean shouldIncludeRepository(RepositoryInfo repository, boolean includePrivate) {
        if (Boolean.TRUE.equals(repository.isPrivate()) && !includePrivate) {
            return false;
        }
        return true;
    }

    /**
     * Validates repository full name format and returns RepositoryFullName value object.
     * 
     * @param fullName Repository full name string
     * @return RepositoryFullName value object
     * @throws IllegalArgumentException if full name format is invalid
     */
    public RepositoryFullName validateAndParseFullName(String fullName) {
        return new RepositoryFullName(fullName);
    }
}
