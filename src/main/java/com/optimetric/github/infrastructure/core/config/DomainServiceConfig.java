package com.optimetric.github.infrastructure.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.optimetric.github.domain.service.RepositoryDomainService;

@Configuration
public class DomainServiceConfig {

    @Bean
    public RepositoryDomainService repositoryDomainService() {
        return new RepositoryDomainService();
    }
}
