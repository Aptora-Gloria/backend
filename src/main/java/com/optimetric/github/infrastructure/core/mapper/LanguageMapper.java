package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LanguageMapper {

    private LanguageMapper() {}

    public static LanguageInfo toLanguageInfo(List<LanguageEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new LanguageInfo(Map.of());
        }
        Map<String, Long> map = entities.stream()
                .collect(Collectors.toMap(
                        LanguageEntity::getLanguageName,
                        LanguageEntity::getBytes,
                        (existing, replacement) -> existing));
        return new LanguageInfo(map);
    }
}
