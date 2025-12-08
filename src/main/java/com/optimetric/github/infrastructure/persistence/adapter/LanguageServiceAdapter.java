package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;
import com.optimetric.github.application.client.LanguageClientPort;
import com.optimetric.github.application.port.LanguageServicePort;
import com.optimetric.github.infrastructure.core.mapper.LanguageMapper;
import com.optimetric.github.infrastructure.persistence.entity.LanguageEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.LanguageRepository;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageServicePort {

    private final LanguageClientPort languageClient;
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
    public LanguageDto getRepoLanguages(String login, Long repositoryId) 
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        // Get repository entity
        RepositoryEntity repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with id: " + repositoryId));

        // Fetch languages from GitHub API
        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        LanguageDto dto = languageClient.getRepoLanguages(accessToken, owner, repoName);

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
