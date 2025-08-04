package com.oobeya.github.mapper;

import com.oobeya.github.dto.response.ContributorStatsDto;
import com.oobeya.github.dto.response.ContributorStatsDto.Week;
import com.oobeya.github.entity.ContributorStatsEntity;
import com.oobeya.github.entity.ContributorWeekStatsEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatsMapper {

    public ContributorStatsEntity toEntity(ContributorStatsDto dto, String ownerLogin, String repoName) {
        ContributorStatsEntity entity = new ContributorStatsEntity();
        entity.setOwnerLogin(ownerLogin);
        entity.setRepoName(repoName);

        if (dto.getAuthor() != null) {
            entity.setAuthorName(dto.getAuthor().getName());
        } else {
            entity.setAuthorName("unknown");
        }

        entity.setTotalCommits(dto.getTotal());

        if (dto.getWeeks() != null && !dto.getWeeks().isEmpty()) {
            List<ContributorWeekStatsEntity> weekEntities = dto.getWeeks().stream().map(week -> {
                ContributorWeekStatsEntity weekEntity = new ContributorWeekStatsEntity();
                weekEntity.setWeek(week.getWeek());
                weekEntity.setAdditions(week.getAdditions());
                weekEntity.setDeletions(week.getDeletions());
                weekEntity.setCommits(week.getCommits());
                weekEntity.setContributorStats(entity); // ilişkiyi kur
                return weekEntity;
            }).collect(Collectors.toList());

            entity.setWeeks(weekEntities);
        } else {
            entity.setWeeks(Collections.emptyList());
        }

        return entity;
    }

    public ContributorStatsDto toDto(ContributorStatsEntity entity) {
        ContributorStatsDto.Author author = new ContributorStatsDto.Author();
        author.setName(entity.getAuthorName());

        ContributorStatsDto dto = new ContributorStatsDto();
        dto.setTotal(entity.getTotalCommits());
        dto.setAuthor(author);

        if (entity.getWeeks() != null && !entity.getWeeks().isEmpty()) {
            List<Week> weeks = entity.getWeeks().stream().map(weekEntity -> {
                Week week = new Week();
                week.setWeek(weekEntity.getWeek());
                week.setAdditions(weekEntity.getAdditions());
                week.setDeletions(weekEntity.getDeletions());
                week.setCommits(weekEntity.getCommits());
                return week;
            }).collect(Collectors.toList());

            dto.setWeeks(weeks);
        } else {
            dto.setWeeks(Collections.emptyList());
        }

        return dto;
    }

    public List<ContributorStatsDto> toDtoList(List<ContributorStatsEntity> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
