package com.oobeya.github.service.impl;

import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.response.LanguageDto;
import com.oobeya.github.dto.response.LanguageUiDto;
import com.oobeya.github.entity.LanguageEntity;
import com.oobeya.github.mapper.LanguageMapper;
import com.oobeya.github.repository.LanguageRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.LanguageServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageServiceI {

    private final ApiClientI apiClient;
    private final AuthService authService;
    private final LanguageRepository repository;

    /**
     * 1️⃣ GitHub'dan dilleri çek ve DB'ye kaydet
     */
    @Override
    public LanguageDto getRepoLanguages(String authHeader, String owner, String repo) {
        String accessToken = authService.getAccessTokenFromToken(authHeader);

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
