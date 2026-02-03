package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.BranchDto;
import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.util.List;

public final class BranchMapper {

    private BranchMapper() {}

    public static BranchDto toDto(BranchEntity e) {
        return new BranchDto(e.getName());
    }

    public static List<BranchDto> toDtos(List<BranchEntity> entities) {
        return entities.stream().map(BranchMapper::toDto).toList();
    }

    public static BranchInfo toBranchInfo(BranchEntity e) {
        return new BranchInfo(e.getName());
    }

    public static List<BranchInfo> toBranchInfos(List<BranchEntity> entities) {
        return entities == null ? List.of() : entities.stream().map(BranchMapper::toBranchInfo).toList();
    }

    // Opsiyonel: Serviste işine yararsa
    public static BranchEntity toEntity(String name, RepositoryEntity repo) {
        return BranchEntity.builder()
                .name(name)
                .repository(repo) // uid @PrePersist/@PreUpdate ile otomatik üretilecek
                .build();
    }
}
