package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.application.port.out.LanguageClientPort;
import com.optimetric.github.application.port.out.LanguageQueryPort;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageQueryPort {

    private final LanguageClientPort languageClient;
    private final AuthServicePort authServicePort;
    private final LanguageRepository languageRepository;
    private final RepositoryPersistenceAdapter repositoryPersistenceAdapter;

    @Override
    @Transactional
    public LanguageInfo initRepoLanguages(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        RepositoryEntity repository = repositoryPersistenceAdapter.findEntityByFullName(repoFullName)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with fullName: " + repoFullName));

        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        LanguageInfo info = languageClient.getRepoLanguages(accessToken, owner, repoName);

        List<LanguageEntity> existingLanguages = languageRepository.findAllByRepository_Id(repository.getId());

        Map<String, Long> languages = info != null ? info.languages() : null;
        if (languages == null || languages.isEmpty()) {
            languageRepository.deleteByRepository_Id(repository.getId());
            return new LanguageInfo(Map.of());
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

            List<LanguageEntity> existingList = languageRepository
                    .findAllByRepository_IdAndLanguageName(repository.getId(), languageName);

            if (!existingList.isEmpty()) {
                // Update first record; remove duplicates if any (data integrity)
                LanguageEntity entity = existingList.get(0);
                entity.setBytes(bytes);
                entity.setPercentage(percentage);
                entity.setLastSyncedAt(now);
                languageRepository.save(entity);
                for (int i = 1; i < existingList.size(); i++) {
                    languageRepository.delete(existingList.get(i));
                }
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

        // GitHub yanıtında artık olmayan dilleri sil
        Set<String> githubLanguageNames = languages.keySet();
        List<LanguageEntity> toDelete = existingLanguages.stream()
                .filter(lang -> !githubLanguageNames.contains(lang.getLanguageName()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            languageRepository.deleteAll(toDelete);
        }

        return new LanguageInfo(languages);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageInfo getLanguages(String repoFullName) {
        List<LanguageEntity> entities = languageRepository.findAllByRepository_FullName(repoFullName);
        return LanguageMapper.toLanguageInfo(entities);
    }
}
