package com.oobeya.github.api.response;

import java.util.List;

public record LanguageUiDto(
        List<LanguageItem> languages,
        long totalBytes
) {
    public record LanguageItem(
            String name,
            long bytes,
            double percentage
    ) {}
}
