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
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageServicePort {

    private final LanguageClientPort languageClient;
    private final AuthServicePort authServicePort;
    private final LanguageRepository languageRepository;
    private final RepositoryPersistenceAdapter repositoryPersistenceAdapter;

    /**
     * Fetches languages from GitHub API and saves them to database.
     * This method is now idempotent: calling it multiple times for the same repository
     * will update existing language records instead of creating duplicates.
     * 
     * @param login User login
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageDto from GitHub API
     */
    @Override
    @Transactional
    public LanguageDto initRepoLanguages(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        // Get repository entity
        RepositoryEntity repository = repositoryPersistenceAdapter.findEntityByFullName(repoFullName)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + repoFullName));

        // Fetch languages from GitHub API
        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        LanguageDto dto = languageClient.getRepoLanguages(accessToken, owner, repoName);

        // Get existing languages from database
        List<LanguageEntity> existingLanguages = languageRepository.findAllByRepository_Id(repository.getId());

        // Calculate total bytes for percentage calculation
        Map<String, Long> languages = dto.languages();
        if (languages == null || languages.isEmpty()) {
            // If GitHub returns no languages, delete all existing ones
            languageRepository.deleteByRepository_Id(repository.getId());
            return dto;
        }

        long totalBytes = languages.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        // Upsert languages: update existing or create new
        OffsetDateTime now = OffsetDateTime.now();
        for (Map.Entry<String, Long> entry : languages.entrySet()) {
            String languageName = entry.getKey();
            Long bytes = entry.getValue();
            BigDecimal percentage = totalBytes > 0 
                    ? BigDecimal.valueOf(bytes * 100.0 / totalBytes)
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            Optional<LanguageEntity> existing = languageRepository
                    .findByRepository_IdAndLanguageName(repository.getId(), languageName);

            if (existing.isPresent()) {
                // Update existing language
                LanguageEntity entity = existing.get();
                entity.setBytes(bytes);
                entity.setPercentage(percentage);
                entity.setLastSyncedAt(now);
                languageRepository.save(entity);
            } else {
                // Create new language
                LanguageEntity entity = LanguageEntity.builder()
                        .languageName(languageName)
                        .bytes(bytes)
                        .percentage(percentage)
                        .repository(repository)
                        .lastSyncedAt(now)
                        .build();
                languageRepository.save(entity);
            }
        }

        // Delete languages that are no longer in GitHub response
        Set<String> githubLanguageNames = languages.keySet();
        List<LanguageEntity> toDelete = existingLanguages.stream()
                .filter(lang -> !githubLanguageNames.contains(lang.getLanguageName()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            languageRepository.deleteAll(toDelete);
        }

        return dto;
    }

    /**
     * Gets languages from database by repository full name (raw Map format).
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageDto
     */
    @Override
    @Transactional(readOnly = true)
    public LanguageDto getLanguagesFromDb(String repoFullName) {
        List<LanguageEntity> entities = languageRepository.findAllByRepository_FullName(repoFullName);
        return LanguageMapper.toDto(entities);
    }

    /**
     * Gets languages from database by repository full name (UI format with percentages).
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageUiDto
     */
    @Override
    @Transactional(readOnly = true)
    public LanguageUiDto getLanguagesForUi(String repoFullName) {
        List<LanguageEntity> entities = languageRepository.findAllByRepository_FullName(repoFullName);
        return LanguageMapper.toUiDto(entities);
    }
}
