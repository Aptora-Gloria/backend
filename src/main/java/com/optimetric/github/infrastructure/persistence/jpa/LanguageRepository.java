package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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

}
