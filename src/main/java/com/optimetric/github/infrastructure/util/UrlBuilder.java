package com.optimetric.github.infrastructure.util;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for building URLs with template replacement and query parameters.
 * Provides a fluent API for constructing URIs.
 * 
 * Example usage:
 * <pre>
 * String url = UrlBuilder.fromTemplate("https://api.github.com/repos/%s/%s/commits", owner, repo)
 *     .addQueryParam("sha", branch)
 *     .addQueryParam("since", since)
 *     .build();
 * </pre>
 */
public class UrlBuilder {

    private final String baseUrl;
    private final Map<String, String> queryParams;

    private UrlBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        this.queryParams = new HashMap<>();
    }

    /**
     * Creates a UrlBuilder from a template string with format placeholders.
     * 
     * @param template URL template with format placeholders (e.g., "https://api.github.com/repos/%s/%s/commits")
     * @param args Arguments to replace placeholders in the template
     * @return UrlBuilder instance
     */
    public static UrlBuilder fromTemplate(String template, Object... args) {
        String url = String.format(template, args);
        return new UrlBuilder(url);
    }

    /**
     * Creates a UrlBuilder from a base URL string.
     * 
     * @param baseUrl Base URL string
     * @return UrlBuilder instance
     */
    public static UrlBuilder fromUrl(String baseUrl) {
        return new UrlBuilder(baseUrl);
    }

    /**
     * Adds a query parameter if the value is not null and not blank.
     * 
     * @param name Parameter name
     * @param value Parameter value
     * @return This UrlBuilder instance for method chaining
     */
    public UrlBuilder addQueryParam(String name, String value) {
        if (name != null && value != null && !value.isBlank()) {
            queryParams.put(name, value);
        }
        return this;
    }

    /**
     * Adds a query parameter if the value is not null.
     * 
     * @param name Parameter name
     * @param value Parameter value (will be converted to string)
     * @return This UrlBuilder instance for method chaining
     */
    public UrlBuilder addQueryParam(String name, Object value) {
        if (name != null && value != null) {
            queryParams.put(name, String.valueOf(value));
        }
        return this;
    }

    /**
     * Adds multiple query parameters from a map.
     * 
     * @param params Map of parameter names to values
     * @return This UrlBuilder instance for method chaining
     */
    public UrlBuilder addQueryParams(Map<String, String> params) {
        if (params != null) {
            params.forEach(this::addQueryParam);
        }
        return this;
    }

    /**
     * Builds the final URI string.
     * 
     * @return URI string
     */
    public String build() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toUriString();
    }

    /**
     * Builds the final URI object.
     * 
     * @return URI object
     */
    public URI buildUri() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toUri();
    }
}


