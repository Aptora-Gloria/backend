package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

import java.time.OffsetDateTime;
import java.util.List;

public final class BranchMapper {

    private BranchMapper() {}

    // Sadece isimle branch (commitCount ve lastCommitDate null olabilir).
    public static BranchInfo toBranchInfo(BranchEntity e) {
        return new BranchInfo(e.getName(), null, null);
    }

    // Metrikler için commit istatistikli branch.
    public static BranchInfo toBranchInfo(BranchEntity e, Long commitCount, OffsetDateTime lastCommitDate) {
        return new BranchInfo(e.getName(), commitCount, lastCommitDate);
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
