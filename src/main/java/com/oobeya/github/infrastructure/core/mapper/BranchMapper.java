package com.oobeya.github.infrastructure.core.mapper;

import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;

import java.util.List;

public final class BranchMapper {

    private BranchMapper() {}

    public static BranchDto toDto(BranchEntity e) {
        return new BranchDto(
                e.getName(),
                e.getRepository().getId()
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
