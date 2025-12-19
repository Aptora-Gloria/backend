package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;
import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LanguageMapper {

    private LanguageMapper() {}

    /**
     * Maps LanguageDto from GitHub API to list of LanguageEntity.
     * Calculates percentage for each language.
     * 
     * @param dto The GitHub API language DTO
     * @param repository The repository entity
     * @return List of LanguageEntity
     */
    public static List<LanguageEntity> toEntities(LanguageDto dto, RepositoryEntity repository) {
        if (dto == null || dto.languages() == null || repository == null) {
            throw new IllegalArgumentException("DTO, languages map, and repository cannot be null");
        }

        Map<String, Long> languages = dto.languages();
        if (languages.isEmpty()) {
            return List.of();
        }

        // Calculate total bytes
        long totalBytes = languages.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        // Map to entities with percentage calculation
        return languages.entrySet().stream()
                .map(entry -> {
                    String languageName = entry.getKey();
                    Long bytes = entry.getValue();
                    BigDecimal percentage = totalBytes > 0 
                            ? BigDecimal.valueOf(bytes * 100.0 / totalBytes)
                                    .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                    return LanguageEntity.builder()
                            .languageName(languageName)
                            .bytes(bytes)
                            .percentage(percentage)
                            .repository(repository)
                            .lastSyncedAt(OffsetDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps list of LanguageEntity to LanguageDto (Map format).
     * 
     * @param entities List of language entities
     * @return LanguageDto
     */
    public static LanguageDto toDto(List<LanguageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new LanguageDto(Map.of());
        }

        Map<String, Long> map = entities.stream()
                .collect(Collectors.toMap(
                        LanguageEntity::getLanguageName,
                        LanguageEntity::getBytes,
                        (existing, replacement) -> existing // In case of duplicates, keep first
                ));
        return new LanguageDto(map);
    }

    /**
     * Maps list of LanguageEntity to LanguageUiDto (UI format with percentages).
     * 
     * @param entities List of language entities
     * @return LanguageUiDto
     */
    public static LanguageUiDto toUiDto(List<LanguageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new LanguageUiDto(List.of(), 0L);
        }

        long totalBytes = entities.stream()
                .mapToLong(LanguageEntity::getBytes)
                .sum();

        List<LanguageUiDto.LanguageItem> items = entities.stream()
                .map(e -> {
                    double percentageValue = e.getPercentage() != null 
                            ? e.getPercentage().doubleValue()
                            : (totalBytes > 0 ? e.getBytes() * 100.0 / totalBytes : 0.0);
                    
                    return new LanguageUiDto.LanguageItem(
                            e.getLanguageName(),
                            e.getBytes(),
                            percentageValue
                    );
                })
                .collect(Collectors.toList());

        return new LanguageUiDto(items, totalBytes);
    }
}
