package com.oobeya.github.api.response;

import java.util.Map;

public record LanguageDto(
        Map<String, Long> languages
) {}
