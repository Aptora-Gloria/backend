package com.oobeya.Gemini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // Bu satırı ekledik!
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiClientConfig {

    @Bean("geminiWebClient")
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}