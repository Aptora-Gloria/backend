package com.oobeya.github.mapper;

import com.oobeya.github.dto.response.CommitDto;
import com.oobeya.github.entity.CommitEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CommitMapper {

    public static CommitEntity toEntity(CommitDto dto, String repoName) {
        return CommitEntity.builder()
                .sha(dto.sha())
                .message(dto.commit().message())
                .authorName(dto.commit().author().name())
                .commitDate(dto.commit().author().date())
                .repoName(repoName)
                .build();
    }

    public static CommitDto toDto(CommitEntity entity) {
        return new CommitDto(
                entity.getSha(),
                new CommitDto.CommitInfo(
                        entity.getMessage(),
                        new CommitDto.CommitInfo.Author(entity.getAuthorName(), entity.getCommitDate())
                )
        );
    }

    public static List<CommitEntity> toEntities(List<CommitDto> list, String repoName) {
        return list.stream().map(dto -> toEntity(dto, repoName)).collect(Collectors.toList());
    }

    public static List<CommitDto> toDtos(List<CommitEntity> list) {
        return list.stream().map(CommitMapper::toDto).collect(Collectors.toList());
    }
}
