package com.optimetric.github.api.mapper;

import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;
import com.optimetric.github.application.dto.LanguageInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LanguageApiMapper {

    private LanguageApiMapper() {}

    public static LanguageDto toLanguageDto(LanguageInfo info) {
        if (info == null || info.languages() == null) {
            return new LanguageDto(Map.of());
        }
        return new LanguageDto(info.languages());
    }

    public static LanguageUiDto toLanguageUiDto(LanguageInfo info) {
        if (info == null || info.languages() == null || info.languages().isEmpty()) {
            return new LanguageUiDto(List.of(), 0L);
        }
        Map<String, Long> map = info.languages();
        long totalBytes = map.values().stream().mapToLong(Long::longValue).sum();
        List<LanguageUiDto.LanguageItem> items = map.entrySet().stream()
                .map(e -> {
                    double percentage = totalBytes > 0 ? e.getValue() * 100.0 / totalBytes : 0.0;
                    return new LanguageUiDto.LanguageItem(e.getKey(), e.getValue(), percentage);
                })
                .collect(Collectors.toList());
        return new LanguageUiDto(items, totalBytes);
    }
}
