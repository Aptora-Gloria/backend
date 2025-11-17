package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.LanguageServicePort;
import com.oobeya.github.infrastructure.core.mapper.LanguageMapper;
import com.oobeya.github.infrastructure.persistence.entity.LanguageEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.jpa.LanguageRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageServicePort {

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final LanguageRepository languageRepository;
    private final RepositoryRepository repositoryRepository;

    /**
     * Fetches languages from GitHub API and saves them to database.
     * 
     * @param login User login
     * @param repositoryId Repository ID
     * @return LanguageDto from GitHub API
     */
    @Override
    @Transactional
    public LanguageDto getRepoLanguages(String login, Long repositoryId) {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        // Get repository entity
        RepositoryEntity repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + repositoryId));

        // Fetch languages from GitHub API
        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        LanguageDto dto = apiClient.getRepoLanguages(accessToken, owner, repoName);

        // Delete old languages for this repository
        languageRepository.deleteByRepository_Id(repositoryId);

        // Save new languages
        List<LanguageEntity> entities = LanguageMapper.toEntities(dto, repository);
        if (!entities.isEmpty()) {
            languageRepository.saveAll(entities);
        }

        return dto;
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
