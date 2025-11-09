package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<BranchEntity, Long> {
    Optional<BranchEntity> findByUid(String uid);
    List<BranchEntity> findAllByRepository_HtmlUrl(String repoHtmlUrl);
    List<BranchEntity> findAllByUidIn(List<String> uids);
}

