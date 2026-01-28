package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;

public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    
    /**
     * Finds all languages by repository ID.
     */
    List<LanguageEntity> findAllByRepository_Id(Long repositoryId);
    
    /**
     * Deletes all languages by repository ID.
     */
    void deleteByRepository_Id(Long repositoryId);
    
    /**
     * Finds all languages by repository full name.
     */
    List<LanguageEntity> findAllByRepository_FullName(String fullName);
    
    /**
     * Deletes all languages by repository full name.
     */
    void deleteByRepository_FullName(String fullName);

    /**
     * Finds a language by repository ID and language name.
     * Used for upsert operations to check if a language already exists.
     */
    Optional<LanguageEntity> findByRepository_IdAndLanguageName(Long repositoryId, String languageName);

    /**
     * Checks if any language exists for the given repository ID.
     * Used for readiness checks (EXISTS with LIMIT 1 for performance).
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
           "FROM LanguageEntity l WHERE l.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);

}
