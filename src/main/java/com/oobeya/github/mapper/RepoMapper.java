package com.oobeya.github.mapper;

import com.oobeya.github.dto.response.RepoDto;
import com.oobeya.github.entity.RepositoryEntity;

import java.util.List;
import java.util.stream.Collectors;

public class RepoMapper {

    public static RepositoryEntity toEntity(RepoDto dto) {
        return RepositoryEntity.builder()
                .name(dto.name())
                .description(dto.description())
                .htmlUrl(dto.htmlUrl())
                .fullName(dto.fullName())
                .fork(dto.fork())
                .build();
    }

    public static List<RepositoryEntity> toEntities(List<RepoDto> list) {
        return list.stream()
                .map(RepoMapper::toEntity)
                .collect(Collectors.toList());
    }

    public static RepoDto toDto(RepositoryEntity entity) {
        return new RepoDto(
                entity.getName(),
                entity.getFullName(),
                entity.getHtmlUrl(),
                entity.getDescription(),
                entity.isFork()
        );
    }
}
