package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.domain.OwnerType;

import java.util.List;
import java.util.Optional;

public interface RepositoryPersistencePort {

    List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, Long userId);

    List<RepositoryInfo> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin);

    List<RepositoryInfo> findByUserWithCriteria(Long userId, OwnerType ownerType, String ownerLogin);

    Optional<RepositoryInfo> findByFullNameAndUser(String fullName, Long userId);

    Optional<RepositoryInfo> findById(Long id);

    List<RepositoryInfo> findAllByHtmlUrlIn(List<String> htmlUrls);

    Optional<RepositoryInfo> findByFullName(String fullName);
}
