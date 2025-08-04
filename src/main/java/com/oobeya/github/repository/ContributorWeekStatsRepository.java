package com.oobeya.github.repository;

import com.oobeya.github.entity.ContributorWeekStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributorWeekStatsRepository extends JpaRepository<ContributorWeekStatsEntity, Long> {
}
