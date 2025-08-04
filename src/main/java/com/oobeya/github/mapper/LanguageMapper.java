package com.oobeya.github.mapper;

import com.oobeya.github.dto.response.LanguageDto;
import com.oobeya.github.dto.response.LanguageUiDto;
import com.oobeya.github.entity.LanguageEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageMapper {

    // DTO -> Entity list
    public static List<LanguageEntity> toEntities(String repo, LanguageDto dto) {
        return dto.languages().entrySet().stream()
                .map(entry -> LanguageEntity.builder()
                        .repoName(repo)
                        .languageName(entry.getKey())
                        .bytes(entry.getValue())
                        .build())
                .toList();
    }

    // Entity list -> DTO
    public static LanguageDto toDto(List<LanguageEntity> entities) {
        Map<String, Long> map = entities.stream()
                .collect(Collectors.toMap(LanguageEntity::getLanguageName, LanguageEntity::getBytes));
        return new LanguageDto(map);
    }

    // Entity list -> UI DTO
    public static LanguageUiDto toUiDto(List<LanguageEntity> entities) {
        long total = entities.stream().mapToLong(LanguageEntity::getBytes).sum();

        List<LanguageUiDto.LanguageItem> items = entities.stream()
                .map(e -> new LanguageUiDto.LanguageItem(
                        e.getLanguageName(),
                        e.getBytes(),
                        total == 0 ? 0.0 : e.getBytes() * 100.0 / total
                ))
                .toList();

        return new LanguageUiDto(items, total);
    }
}
