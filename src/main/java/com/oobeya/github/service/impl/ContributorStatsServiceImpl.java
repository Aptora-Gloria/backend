package com.oobeya.github.service.impl;

import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.ContributorStatsDto;
import com.oobeya.github.entity.ContributorStatsEntity;
import com.oobeya.github.mapper.StatsMapper;
import com.oobeya.github.repository.ContributorStatsRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.ContributorStatsServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributorStatsServiceImpl implements ContributorStatsServiceI {

    private final ApiClientI apiClient;
    private final ContributorStatsRepository statsRepository;
    private final StatsMapper statsMapper;
    private final AuthService authService;

    @Override
    public List<ContributorStatsDto> fetchAndSaveContributorStats(String authHeader, String owner, String repo) {
        String accessToken = authService.getAccessTokenFromToken(authHeader);
        List<ContributorStatsDto> stats = apiClient.fetchContributorStats(accessToken, owner, repo);

        List<ContributorStatsEntity> entities = stats.stream()
                .map(dto -> statsMapper.toEntity(dto, owner, repo))
                .collect(Collectors.toList());

        statsRepository.saveAll(entities);
        return stats;
    }

    @Override
    public List<ContributorStatsDto> getContributorStats(String authHeader, String owner, String repo) {
        List<ContributorStatsEntity> entities =
                statsRepository.findByOwnerLoginAndRepoName(owner, repo);
        return statsMapper.toDtoList(entities);
    }
}
