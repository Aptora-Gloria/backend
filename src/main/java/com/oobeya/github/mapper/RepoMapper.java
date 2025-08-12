package com.oobeya.github.mapper;


import com.oobeya.github.dto.response.RepoDto;
import com.oobeya.github.entity.RepositoryEntity;

import java.util.List;
import java.util.stream.Collectors;

public class RepoMapper {

    public static RepositoryEntity toEntity(RepoDto dto, String ownerLogin) {
        return RepositoryEntity.builder()
                .name(dto.name())
                .fullName(dto.fullName())
                .description(dto.description())
                .htmlUrl(dto.htmlUrl())
                .fork(dto.fork())
                .login(ownerLogin)
                .build();
    }

    public static RepoDto toDto(RepositoryEntity entity) {


        return new RepoDto(
                entity.getName(),
                entity.getFullName(),
                entity.getHtmlUrl(),
                entity.getDescription(),
                entity.isFork(),
                entity.getLogin()

        );
    }

    public static List<RepositoryEntity> toEntities(List<RepoDto> list, String ownerLogin) {
        return list.stream()
                .map(dto -> toEntity(dto, ownerLogin))
                .collect(Collectors.toList());
    }

    public static List<RepoDto> toDtos(List<RepositoryEntity> list) {
        return list.stream()
                .map(RepoMapper::toDto)
                .collect(Collectors.toList());
    }
}
