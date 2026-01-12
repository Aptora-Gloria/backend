package com.optimetric.github.infrastructure.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.optimetric.github.domain.service.RepositoryDomainService;

/**
 * Configuration class for domain services.
 * 
 * This configuration registers domain services as Spring beans without
 * polluting the domain layer with framework annotations.
 * 
 * Domain services remain framework-agnostic (no Spring dependencies).
 */
@Configuration
public class DomainServiceConfig {

    /**
     * Registers RepositoryDomainService as a Spring bean.
     * 
     * @return RepositoryDomainService instance
     */
    @Bean
    public RepositoryDomainService repositoryDomainService() {
        return new RepositoryDomainService();
    }
}
