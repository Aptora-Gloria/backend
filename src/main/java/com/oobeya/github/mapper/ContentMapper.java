package com.oobeya.github.mapper;

import com.oobeya.github.dto.ContentDto;
import com.oobeya.github.entity.ContentEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ContentMapper {

    // DTO → Entity
    public static ContentEntity toEntity(ContentDto dto, String repoName) {
        return ContentEntity.builder()
                .name(dto.name())
                .path(dto.path())
                .sha(dto.sha())
                .type(dto.type())
                .downloadUrl(dto.downloadUrl())
                .repoName(repoName)
                .build();
    }

    // Entity → DTO
    public static ContentDto toDto(ContentEntity entity) {
        return new ContentDto(
                entity.getName(),
                entity.getContent(),
                entity.getPath(),
                entity.getSha(),
                entity.getType(),
                entity.getDownloadUrl(),
                entity.getEncoding()
        );
    }

    // Liste dönüşümleri
    public static List<ContentEntity> toEntities(List<ContentDto> list, String repoName) {
        return list.stream()
                .map(dto -> toEntity(dto, repoName))
                .collect(Collectors.toList());
    }

    public static List<ContentDto> toDtos(List<ContentEntity> list) {
        return list.stream()
                .map(ContentMapper::toDto)
                .collect(Collectors.toList());
    }
}
