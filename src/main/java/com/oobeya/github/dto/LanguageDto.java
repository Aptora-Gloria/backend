package com.oobeya.github.dto;

import java.util.Map;

public record LanguageDto(
        Map<String, Long> languages
) {}
