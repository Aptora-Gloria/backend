package com.optimetric.github.application.port;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.domain.OwnerType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for repository persistence operations.
 * This port abstracts database access from the application layer.
 */
public interface RepositoryPersistencePort {

    /**
     * Saves all repositories. Implements upsert logic by checking existing repositories
     * by htmlUrl and only saving new ones.
     * 
     * @param repos List of repository initialization data
     * @param user User to associate repositories with
     * @return List of saved repository information
     */
    List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, User user);

    /**
     * Finds all repositories by owner login, ordered by pushed date descending.
     * 
     * @param ownerLogin Owner login
     * @return List of repository information
     */
    List<RepositoryInfo> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin);

    /**
     * Finds repositories by user with optional filters, ordered by pushed date descending.
     * 
     * @param user User
     * @param ownerType Owner type filter (optional)
     * @param ownerLogin Owner login filter (optional)
     * @return List of repository information
     */
    List<RepositoryInfo> findByUserWithCriteria(User user, OwnerType ownerType, String ownerLogin);

    /**
     * Finds repository by full name and user.
     * 
     * @param fullName Repository full name (e.g., "owner/repo")
     * @param user User
     * @return Optional repository information
     */
    Optional<RepositoryInfo> findByFullNameAndUser(String fullName, User user);

    /**
     * Finds repository by ID.
     * Used by other adapters that need entity access.
     * 
     * @param id Repository ID
     * @return Optional repository information
     */
    Optional<RepositoryInfo> findById(Long id);

    /**
     * Finds all repositories by HTML URLs.
     * Used for deduplication during save operations.
     * 
     * @param htmlUrls List of HTML URLs
     * @return List of repository information
     */
    List<RepositoryInfo> findAllByHtmlUrlIn(List<String> htmlUrls);

    /**
     * Finds repository by full name.
     * 
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return Optional repository information
     */
    Optional<RepositoryInfo> findByFullName(String fullName);
}

