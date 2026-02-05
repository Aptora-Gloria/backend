package com.optimetric.github.infrastructure.util;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UrlBuilder {

    private final String baseUrl;
    private final Map<String, String> queryParams;

    private UrlBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        this.queryParams = new HashMap<>();
    }

    public static UrlBuilder fromTemplate(String template, Object... args) {
        String url = String.format(template, args);
        return new UrlBuilder(url);
    }

    public static UrlBuilder fromUrl(String baseUrl) {
        return new UrlBuilder(baseUrl);
    }

    public UrlBuilder addQueryParam(String name, String value) {
        if (name != null && value != null && !value.isBlank()) {
            queryParams.put(name, value);
        }
        return this;
    }

    public UrlBuilder addQueryParam(String name, Object value) {
        if (name != null && value != null) {
            queryParams.put(name, String.valueOf(value));
        }
        return this;
    }

    public UrlBuilder addQueryParams(Map<String, String> params) {
        if (params != null) {
            params.forEach(this::addQueryParam);
        }
        return this;
    }

    public String build() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toUriString();
    }

    public URI buildUri() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toUri();
    }
}



