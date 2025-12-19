package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    
    /**
     * Finds all languages by repository ID.
     */
    List<LanguageEntity> findAllByRepository_Id(Long repositoryId);
    
    /**
     * Deletes all languages by repository ID.
     */
    void deleteByRepository_Id(Long repositoryId);
    

}
