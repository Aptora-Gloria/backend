package com.optimetric.github.application.dto;

import java.util.Map;

/**
 * Application DTO for repository language information.
 */
public record LanguageInfo(Map<String, Long> languages) {}
