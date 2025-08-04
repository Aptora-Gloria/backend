package com.oobeya.github.dto.response;

import java.util.Map;

public record LanguageDto(
        Map<String, Long> languages
) {}
