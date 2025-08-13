package com.oobeya.github.mapper;


import com.oobeya.github.dto.response.BranchDto;
import com.oobeya.github.entity.BranchEntity;
import com.oobeya.github.entity.RepositoryEntity;

import java.util.List;

public final class BranchMapper {

    private BranchMapper() {}

    public static BranchDto toDto(BranchEntity e) {
        return new BranchDto(
                e.getName(),
                e.getUid(),
                e.getRepository().getHtmlUrl()
        );
    }

    public static List<BranchDto> toDtos(List<BranchEntity> entities) {
        return entities.stream().map(BranchMapper::toDto).toList();
    }

    // Opsiyonel: Serviste işine yararsa
    public static BranchEntity toEntity(String name, RepositoryEntity repo) {
        return BranchEntity.builder()
                .name(name)
                .repository(repo) // uid @PrePersist/@PreUpdate ile otomatik üretilecek
                .build();
    }
}
