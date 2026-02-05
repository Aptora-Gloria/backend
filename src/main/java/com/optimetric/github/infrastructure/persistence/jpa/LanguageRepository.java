package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;

public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    
    List<LanguageEntity> findAllByRepository_Id(Long repositoryId);
    
    void deleteByRepository_Id(Long repositoryId);
    
    List<LanguageEntity> findAllByRepository_FullName(String fullName);
    
    void deleteByRepository_FullName(String fullName);

    Optional<LanguageEntity> findByRepository_IdAndLanguageName(Long repositoryId, String languageName);

    List<LanguageEntity> findAllByRepository_IdAndLanguageName(Long repositoryId, String languageName);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
           "FROM LanguageEntity l WHERE l.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);

}
