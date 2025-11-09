package com.oobeya.github.infrastructure.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.github")
public class GitHubOAuthProperties {
    private String clientId;
    private String clientSecret;
}
