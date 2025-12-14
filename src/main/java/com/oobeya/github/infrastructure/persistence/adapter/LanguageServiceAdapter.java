package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.LanguageServicePort;
import com.oobeya.github.dto.request.LanguageSyncRequest;
import com.oobeya.github.infrastructure.core.mapper.LanguageMapper;
import com.oobeya.github.infrastructure.persistence.entity.LanguageEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.jpa.LanguageRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.oobeya.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageServicePort {

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final LanguageRepository languageRepository;
    private final RepositoryRepository repositoryRepository;

    /**
     * Synchronizes languages from GitHub API and saves them to database.
     * 
     * @param request Language sync request containing repository info and auth header
     * @return LanguageDto from GitHub API
     */
    @Override
    @Transactional
    public LanguageDto syncLanguages(LanguageSyncRequest request) {
        try {
            RepositoryEntity repository = repositoryRepository.findById(request.repositoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Repository not found with id: " + request.repositoryId()));

            String accessToken = authServicePort.getAccessTokenFromToken(request.authHeader());
            if (accessToken == null || accessToken.isEmpty()) {
                throw new GitHubApiException("Access token is required for syncing languages");
            }

            // Fetch languages from GitHub API using request parameters
            LanguageDto dto = apiClient.getRepoLanguages(accessToken, request.owner(), request.repositoryName());

            // Delete old languages for this repository
            languageRepository.deleteByRepository_Id(request.repositoryId());

            // Save new languages
            List<LanguageEntity> entities = LanguageMapper.toEntities(dto, repository);
            if (!entities.isEmpty()) {
                languageRepository.saveAll(entities);
            }

            log.info("Successfully synced languages for repository {} ({}/{})",
                    request.repositoryId(), request.owner(), request.repositoryName());

            return dto;
        } catch (GitHubApiException e) {
            log.error("GitHub API error while syncing languages: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error syncing languages for repository {}: {}",
                    request.repositoryId(), e.getMessage(), e);
            throw new GitHubApiException("Failed to sync languages: " + e.getMessage(), e);
        }
    }

    /**
     * Gets languages from database by repository ID (raw Map format).
     * 
     * @param repositoryId Repository ID
     * @return LanguageDto
     */
    @Override
    @Transactional(readOnly = true)
    public LanguageDto getLanguagesFromDb(Long repositoryId) {
        List<LanguageEntity> entities = languageRepository.findAllByRepository_Id(repositoryId);
        return LanguageMapper.toDto(entities);
    }

    /**
     * Gets languages from database by repository ID (UI format with percentages).
     * 
     * @param repositoryId Repository ID
     * @return LanguageUiDto
     */
    @Override
    @Transactional(readOnly = true)
    public LanguageUiDto getLanguagesForUi(Long repositoryId) {
        List<LanguageEntity> entities = languageRepository.findAllByRepository_Id(repositoryId);
        return LanguageMapper.toUiDto(entities);
    }
}
