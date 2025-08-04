package com.oobeya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan; // Bu satırı ekleyin

import com.oobeya.github.config.GitHubOAuthProperties;

// @SpringBootApplication altında, taranacak paketleri belirtiyoruz.
@SpringBootApplication
@EnableConfigurationProperties(GitHubOAuthProperties.class)
@ComponentScan(basePackages = {
		"com.oobeya.github",
		"com.oobeya.Gemini",
		"com.oobeya.auth"
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}

