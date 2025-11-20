package com.optimetric;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan; // Bu satırı ekleyin

import com.optimetric.github.infrastructure.core.config.GitHubOAuthProperties;

// @SpringBootApplication altında, taranacak paketleri belirtiyoruz.
@SpringBootApplication
@EnableConfigurationProperties(GitHubOAuthProperties.class)
@ComponentScan(basePackages = {
		"com.optimetric.github",
		"com.optimetric.Gemini",
		"com.optimetric.auth"
	})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}

