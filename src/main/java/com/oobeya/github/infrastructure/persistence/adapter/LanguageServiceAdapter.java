package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.infrastructure.persistence.entity.LanguageEntity;
import com.oobeya.github.infrastructure.core.mapper.LanguageMapper;
import com.oobeya.github.infrastructure.persistence.jpa.LanguageRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.LanguageServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceAdapter implements LanguageServicePort {

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final LanguageRepository repository;

    /**
     * 1️⃣ GitHub'dan dilleri çek ve DB'ye kaydet
     */
    @Override
    public LanguageDto getRepoLanguages(String authHeader, String owner, String repo) {
        String accessToken = authServicePort.getAccessTokenFromToken(authHeader);

        // GitHub'dan veriyi çek
        LanguageDto dto = apiClient.getRepoLanguages(accessToken, owner, repo);

        // Önce eski veriyi sil
        repository.deleteByRepoName(repo);

        // Yeni veriyi kaydet
        List<LanguageEntity> entities = LanguageMapper.toEntities(repo, dto);
        repository.saveAll(entities);

        return dto;
    }

    /**
     * 2️⃣ DB'den ham Map formatında getir
     */
    public LanguageDto getLanguagesFromDb(String repo) {
        List<LanguageEntity> entities = repository.findByRepoName(repo);
        return LanguageMapper.toDto(entities);
    }

    /**
     * 3️⃣ DB'den UI'ye uygun (yüzde + toplam) formatta getir
     */
    public LanguageUiDto getLanguagesForUi(String repo) {
        List<LanguageEntity> entities = repository.findByRepoName(repo);
        return LanguageMapper.toUiDto(entities);
    }
}
