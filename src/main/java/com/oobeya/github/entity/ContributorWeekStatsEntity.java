package com.oobeya.github.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "contributor_week_stats")
public class ContributorWeekStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long week;
    private int additions;
    private int deletions;
    private int commits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_stats_id")
    private ContributorStatsEntity contributorStats;
}
