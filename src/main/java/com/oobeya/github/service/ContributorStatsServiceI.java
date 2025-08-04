package com.oobeya.github.service;

import com.oobeya.github.dto.response.ContributorStatsDto;

import java.util.List;

public interface ContributorStatsServiceI {
    List<ContributorStatsDto> fetchAndSaveContributorStats(String token, String owner, String repo);
    List<ContributorStatsDto> getContributorStats(String token, String owner, String repo);
}
